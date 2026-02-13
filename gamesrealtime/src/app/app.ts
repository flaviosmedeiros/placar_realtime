import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { GameStatus, game } from './models/game.model';
import { GameSseService } from './services/game-sse.service';


@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly jogosNovos = signal<game[]>([]);
  protected readonly jogosEmAndamento = signal<game[]>([]);
  protected readonly jogosEncerrados = signal<game[]>([]);
  protected readonly GameStatus = GameStatus;
  protected readonly relogio = signal(Date.now());

  private readonly formatter = new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short'
  });
  private readonly referenciasTempo = new Map<string, { segundosBase: number; sincronizadoEm: number }>();
  private relogioIntervalo: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly sse: GameSseService) { }

  ngOnInit(): void {
    this.iniciarRelogio();
    this.sse.connect({
      onNovo: (update) => this.receberNovoJogo(update),
      onInicio: (update) => this.iniciarJogo(update),
      onPlacar: (update) => this.atualizarPlacar(update),
      onEncerrado: (update) => this.encerrarJogo(update),
      onExcluido: (update) => this.excluirJogoNovo(update),
      onError: () => {
        console.error('Erro ao receber informações das partidas');
      }
    });
  }

  ngOnDestroy(): void {
    this.pararRelogio();
    this.sse.close();
  }

  protected formatarData(value?: Date | null): string {
    if (!value || Number.isNaN(value.getTime())) {
      return '-';
    }

    return this.formatter.format(value);
  }

  protected formatarTempoDeJogo(jogo: game): string {
    const agora = this.relogio();
    const segundosTotais = this.calcularSegundosTotais(jogo, agora);
    const minutos = Math.floor(segundosTotais / 60);
    const segundos = segundosTotais % 60;
    return `${String(minutos).padStart(2, '0')}:${String(segundos).padStart(2, '0')}`;
  }

  protected formatarStatus(status?: string | null): string {
    switch (status) {
      case GameStatus.NAO_INICIADO:
        return 'Não iniciado';
      case GameStatus.EM_ANDAMENTO:
        return 'Em andamento';
      case GameStatus.FINALIZADO:
        return 'Finalizado';
      default:
        return status ?? '-';
    }
  }


  private receberNovoJogo(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.NAO_INICIADO);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.NAO_INICIADO);
    this.moverJogo(jogo, GameStatus.NAO_INICIADO);
  }

  private iniciarJogo(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.EM_ANDAMENTO);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private atualizarPlacar(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.EM_ANDAMENTO);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private encerrarJogo(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.FINALIZADO);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.FINALIZADO);
    this.moverJogo(jogo, GameStatus.FINALIZADO);
  }

  private excluirJogoNovo(update: game): void {
    this.jogosNovos.update((lista) => this.removerPorId(lista, update.id));
    this.referenciasTempo.delete(this.normalizarId(update.id));
  }

  private iniciarRelogio(): void {
    if (this.relogioIntervalo) {
      return;
    }
    this.relogioIntervalo = setInterval(() => {
      this.relogio.set(Date.now());
    }, 1000);
  }

  private pararRelogio(): void {
    if (!this.relogioIntervalo) {
      return;
    }
    clearInterval(this.relogioIntervalo);
    this.relogioIntervalo = null;
  }

  private atualizarReferenciaTempo(update: game, jogo: game, status: GameStatus): void {
    if (status !== GameStatus.EM_ANDAMENTO) {
      this.referenciasTempo.delete(jogo.id);
      return;
    }

    if (this.ehNumeroValido(update.tempoDeJogo)) {
      this.sincronizarTempo(jogo.id, update.tempoDeJogo);
      return;
    }

    if (!this.referenciasTempo.has(jogo.id)) {
      this.sincronizarTempo(jogo.id, jogo.tempoDeJogo);
    }
  }

  private sincronizarTempo(id: string, tempoDeJogo: number): void {
    const segundosBase = this.converterMinutosParaSegundos(tempoDeJogo);
    this.referenciasTempo.set(id, { segundosBase, sincronizadoEm: Date.now() });
  }

  private calcularSegundosTotais(jogo: game, agora: number): number {
    if (jogo.status !== GameStatus.EM_ANDAMENTO) {
      return this.converterMinutosParaSegundos(jogo.tempoDeJogo);
    }

    const referencia = this.referenciasTempo.get(jogo.id);
    if (!referencia) {
      return this.converterMinutosParaSegundos(jogo.tempoDeJogo);
    }

    const decorrido = Math.max(0, Math.floor((agora - referencia.sincronizadoEm) / 1000));
    return referencia.segundosBase + decorrido;
  }

  private converterMinutosParaSegundos(tempoDeJogo: number): number {
    if (!this.ehNumeroValido(tempoDeJogo)) {
      return 0;
    }
    return Math.max(0, Math.floor(tempoDeJogo * 60));
  }

  private ehNumeroValido(valor: unknown): valor is number {
    return typeof valor === 'number' && Number.isFinite(valor);
  }  

  private criarJogo(update: game, statusPadrao: GameStatus): game {
    update.status = statusPadrao;
    update.dataHoraInicioPartida = this.criarData(update.dataHoraInicioPartida);
    update.dataHoraEncerramento = this.criarData(update.dataHoraEncerramento);
    update.dataHoraEncerramento = this.criarData(update.dataHoraEncerramento);
    return update;
  }

  private criarData(valor: unknown): Date {
    if (valor instanceof Date) {
      return valor;
    }

    if (!valor) {
      return new Date('');
    }

    return new Date(String(valor));
  }

  private moverJogo(jogo: game, destino: GameStatus): void {
    this.jogosNovos.update((lista) => this.removerPorId(lista, jogo.id));
    this.jogosEmAndamento.update((lista) => this.removerPorId(lista, jogo.id));
    this.jogosEncerrados.update((lista) => this.removerPorId(lista, jogo.id));
    
    switch (destino) {
      case GameStatus.NAO_INICIADO:
        this.jogosNovos.update((lista) => this.adicionarOuAtualizar(lista, jogo));
        break;
      case GameStatus.EM_ANDAMENTO:
        this.jogosEmAndamento.update((lista) => this.adicionarOuAtualizar(lista, jogo));
        break;
      case GameStatus.FINALIZADO:
        this.jogosEncerrados.update((lista) => this.adicionarOuAtualizar(lista, jogo));
        break;
    }
  }

  private removerPorId(lista: game[], id: string): game[] {
    const listaLimpa = lista.filter((jogo, index, self) =>
        index === self.findIndex((g) => this.mesmoId(g.id ,jogo.id))
    );
    return listaLimpa;
  }


  private adicionarOuAtualizar(lista: game[], jogo: game): game[] {
    const semDuplicados = this.removerPorId(lista, jogo.id);
    return [...semDuplicados, jogo];
  } 

  private mesmoId(idA: unknown, idB: unknown): boolean {
    return this.normalizarId(idA) === this.normalizarId(idB);
  }

  private normalizarId(id: unknown): string {
    return String(id).trim();
  }
}
