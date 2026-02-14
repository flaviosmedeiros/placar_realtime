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
  private readonly referenciaTempoPorJogo = new Map<string, { baseSegundos: number; instanteEventoMs: number; congelado: boolean }>();

  private readonly formatter = new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short'
  });


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
      onError: () => console.error('Erro ao receber informações das partidas')
    });

    // TODO envia request para a api rest-consumer para disparar 
    // a emissão das informações sobre os jogos com status não iniciados e em adamento
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
    if (jogo.status === GameStatus.NAO_INICIADO) {
      return '-';
    }

    const agora = this.relogio();
    const referencia = this.referenciaTempoPorJogo.get(this.normalizarId(jogo.id));

    if (referencia) {
      const acrescimo = referencia.congelado
        ? 0
        : Math.max(0, Math.floor((agora - referencia.instanteEventoMs) / 1000));
      return this.formatarMmSs(referencia.baseSegundos + acrescimo);
    }

    const inicio = jogo.dataHoraInicioPartida;
    if (!(inicio instanceof Date) || Number.isNaN(inicio.getTime())) {
      return this.formatarMmSs(this.converterMinutosParaSegundos(jogo.tempoDeJogo));
    }

    const segundosTotais = Math.max(0, Math.floor((agora - inicio.getTime()) / 1000));
    return this.formatarMmSs(segundosTotais);
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
    this.referenciaTempoPorJogo.delete(jogo.id);
    this.moverJogo(jogo, GameStatus.NAO_INICIADO);
  }

  private iniciarJogo(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO);
    this.registrarTempoNoEvento(jogo, GameStatus.EM_ANDAMENTO);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private atualizarPlacar(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO);
    this.registrarTempoNoEvento(jogo, GameStatus.EM_ANDAMENTO);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private encerrarJogo(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.FINALIZADO);
    this.registrarTempoNoEvento(jogo, GameStatus.FINALIZADO);
    this.moverJogo(jogo, GameStatus.FINALIZADO);
  }


  private excluirJogoNovo(update: game): void {
    const jogo = this.criarJogo(update, GameStatus.EXCLUIDO);
    this.referenciaTempoPorJogo.delete(jogo.id);
    this.removeJogoFromListas(jogo.id);
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




  private converterMinutosParaSegundos(tempoDeJogo: number): number {
    if (!this.ehNumeroValido(tempoDeJogo)) {
      return 0;
    }
    return Math.max(0, Math.floor(tempoDeJogo * 60));
  }

  private formatarMmSs(segundosTotais: number): string {
    const minutos = Math.floor(segundosTotais / 60);
    const segundos = segundosTotais % 60;
    return `${String(minutos).padStart(2, '0')}:${String(segundos).padStart(2, '0')}`;
  }

  private registrarTempoNoEvento(jogo: game, status: GameStatus): void {
    const instanteEventoMs = Date.now();
    const id = this.normalizarId(jogo.id);
    const baseSegundos = this.calcularTempoNoMomentoDoEvento(jogo, instanteEventoMs);

    this.referenciaTempoPorJogo.set(id, {
      baseSegundos,
      instanteEventoMs,
      congelado: status === GameStatus.FINALIZADO
    });
  }

  private calcularTempoNoMomentoDoEvento(jogo: game, instanteEventoMs: number): number {
    if (jogo.status === GameStatus.FINALIZADO) {
      const inicio = jogo.dataHoraInicioPartida;
      const encerramento = jogo.dataHoraEncerramento;
      if (
        inicio instanceof Date &&
        !Number.isNaN(inicio.getTime()) &&
        encerramento instanceof Date &&
        !Number.isNaN(encerramento.getTime())
      ) {
        return Math.max(0, Math.floor((encerramento.getTime() - inicio.getTime()) / 1000));
      }
    }

    const inicio = jogo.dataHoraInicioPartida;
    if (inicio instanceof Date && !Number.isNaN(inicio.getTime())) {
      return Math.max(0, Math.floor((instanteEventoMs - inicio.getTime()) / 1000));
    }

    return this.converterMinutosParaSegundos(jogo.tempoDeJogo);
  }


  private ehNumeroValido(valor: unknown): valor is number {
    return typeof valor === 'number' && Number.isFinite(valor);
  }



  private criarJogo(update: game, statusPadrao: GameStatus): game {
    update.status = statusPadrao;
    update.dataHoraInicioPartida = this.criarData(update.dataHoraInicioPartida);
    update.dataHoraEncerramento = this.criarData(update.dataHoraEncerramento);
    update.id = this.normalizarId(update.id);
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
  
    this.removeJogoFromListas(jogo.id);
   
    // 2. Adiciona apenas na lista de destino
    switch (destino) {
      case GameStatus.NAO_INICIADO:
        this.jogosNovos.update(lista => [...lista, jogo]);
        break;
      case GameStatus.EM_ANDAMENTO:
        this.jogosEmAndamento.update(lista => [...lista, jogo]);
        break;
      case GameStatus.FINALIZADO:
        this.jogosEncerrados.update(lista => [...lista, jogo]);
        break;
    }
  }

  // Helper simplificado e funcional
  private mesmoId(idA: any, idB: any): boolean {
    return String(idA).trim() === String(idB).trim();
  }


  private normalizarId(id: unknown): string {
    return String(id).trim();
  }

  private removeJogoFromListas(jogoId: string): void {
    this.jogosNovos.update(lista => lista.filter(g => !this.mesmoId(g.id, jogoId)));
    this.jogosEmAndamento.update(lista => lista.filter(g => !this.mesmoId(g.id, jogoId)));
    this.jogosEncerrados.update(lista => lista.filter(g => !this.mesmoId(g.id, jogoId)));
  }
}
