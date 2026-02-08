import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { GameStatus, game } from './models/game.model';
import { GameSseService } from './services/game-sse.service';

type GameUpdate = Partial<game> & { id: string };

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


  private receberNovoJogo(update: GameUpdate): void {
    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.NAO_INICIADO, base);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.NAO_INICIADO);
    this.moverJogo(jogo, GameStatus.NAO_INICIADO);
  }

  private iniciarJogo(update: GameUpdate): void {
    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO, base);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.EM_ANDAMENTO);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private atualizarPlacar(update: GameUpdate): void {
    const lista = this.jogosEmAndamento();
    const indice = lista.findIndex((item) => item.id === update.id);

    if (indice >= 0) {
      const atualizado = this.criarJogo(update, GameStatus.EM_ANDAMENTO, lista[indice]);
      this.atualizarReferenciaTempo(update, atualizado, GameStatus.EM_ANDAMENTO);
      const proxima = [...lista];
      proxima[indice] = atualizado;
      this.jogosEmAndamento.set(proxima);
      return;
    }

    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO, base);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.EM_ANDAMENTO);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private encerrarJogo(update: GameUpdate): void {
    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.FINALIZADO, base);
    this.atualizarReferenciaTempo(update, jogo, GameStatus.FINALIZADO);
    this.moverJogo(jogo, GameStatus.FINALIZADO);
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

  private atualizarReferenciaTempo(update: GameUpdate, jogo: game, status: GameStatus): void {
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

  private buscarJogo(id: string): game | null {
    return (
      this.jogosNovos().find((item) => item.id === id) ??
      this.jogosEmAndamento().find((item) => item.id === id) ??
      this.jogosEncerrados().find((item) => item.id === id) ??
      null
    );
  }

  private criarJogo(update: GameUpdate, statusPadrao: GameStatus, base: game | null): game {
    return {
      id: update.id,
      timeA: update.timeA ?? base?.timeA ?? 'Time A',
      timeB: update.timeB ?? base?.timeB ?? 'Time B',
      placarA: update.placarA ?? base?.placarA ?? 0,
      placarB: update.placarB ?? base?.placarB ?? 0,
      status: update.status ?? base?.status ?? statusPadrao,
      tempoDeJogo: update.tempoDeJogo ?? base?.tempoDeJogo ?? 0,
      dataHoraInicioPartida: this.criarData(update.dataHoraInicioPartida ?? base?.dataHoraInicioPartida),
      dataHoraEncerramento: this.criarData(update.dataHoraEncerramento ?? base?.dataHoraEncerramento)
    };
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

    const destinoComStatus = { ...jogo, status: destino };

    switch (destino) {
      case GameStatus.NAO_INICIADO:
        this.jogosNovos.update((lista) => this.adicionarOuAtualizar(lista, destinoComStatus));
        break;
      case GameStatus.EM_ANDAMENTO:
        this.jogosEmAndamento.update((lista) => this.adicionarOuAtualizar(lista, destinoComStatus));
        break;
      case GameStatus.FINALIZADO:
        this.jogosEncerrados.update((lista) => this.adicionarOuAtualizar(lista, destinoComStatus));
        break;
    }
  }

  private removerPorId(lista: game[], id: string): game[] {
    const proxima = lista.filter((item) => item.id !== id);
    return proxima.length === lista.length ? lista : proxima;
  }

  private adicionarOuAtualizar(lista: game[], jogo: game): game[] {
    const indice = lista.findIndex((item) => item.id === jogo.id);

    if (indice < 0) {
      return [...lista, jogo];
    }

    const proxima = [...lista];
    proxima[indice] = jogo;
    return proxima;
  }
}
