import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { GameStatus, game, logGame } from './models/game.model';
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
  protected readonly modalAberto = signal(false);
  protected readonly logsSelecionados = signal<logGame[]>([]);
  protected readonly logsCarregando = signal(false);
  protected readonly logsErro = signal<string | null>(null);
  protected readonly jogoSelecionado = signal<game | null>(null);
  protected readonly GameStatus = GameStatus;

  private readonly formatter = new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short'
  });

  constructor(private readonly sse: GameSseService) {}

  ngOnInit(): void {
    this.sse.connect({
      onNovo: (update) => this.receberNovoJogo(update),
      onInicio: (update) => this.iniciarJogo(update),
      onPlacar: (update) => this.atualizarPlacar(update),
      onEncerrado: (update) => this.encerrarJogo(update),
      onError: () => {
        console.error('Erro ao receber eventos SSE');
      }
    });
  }

  ngOnDestroy(): void {
    this.sse.close();
  }

  protected formatarData(value?: Date | null): string {
    if (!value || Number.isNaN(value.getTime())) {
      return '-';
    }

    return this.formatter.format(value);
  }

  protected labelStatus(status: string): string {
    switch (status) {
      case GameStatus.NAO_INICIADO:
        return 'Nao iniciado';
      case GameStatus.EM_ANDAMENTO:
        return 'Em andamento';
      case GameStatus.ENCERRADO:
        return 'Encerrado';
      default:
        return status;
    }
  }

  protected async abrirLogs(jogo: game): Promise<void> {
    this.modalAberto.set(true);
    this.logsCarregando.set(true);
    this.logsErro.set(null);
    this.logsSelecionados.set([]);
    this.jogoSelecionado.set(jogo);

    try {
      const logs = await this.sse.fetchLogs(jogo.id);
      this.logsSelecionados.set(logs);
    } catch (error) {
      console.error(error);
      this.logsErro.set('Nao foi possivel carregar os logs.');
    } finally {
      this.logsCarregando.set(false);
    }
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
    this.logsSelecionados.set([]);
    this.logsErro.set(null);
    this.jogoSelecionado.set(null);
  }

  private receberNovoJogo(update: GameUpdate): void {
    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.NAO_INICIADO, base);
    this.moverJogo(jogo, GameStatus.NAO_INICIADO);
  }

  private iniciarJogo(update: GameUpdate): void {
    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO, base);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private atualizarPlacar(update: GameUpdate): void {
    const lista = this.jogosEmAndamento();
    const indice = lista.findIndex((item) => item.id === update.id);

    if (indice >= 0) {
      const atualizado = this.criarJogo(update, GameStatus.EM_ANDAMENTO, lista[indice]);
      const proxima = [...lista];
      proxima[indice] = atualizado;
      this.jogosEmAndamento.set(proxima);
      return;
    }

    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.EM_ANDAMENTO, base);
    this.moverJogo(jogo, GameStatus.EM_ANDAMENTO);
  }

  private encerrarJogo(update: GameUpdate): void {
    const base = this.buscarJogo(update.id);
    const jogo = this.criarJogo(update, GameStatus.ENCERRADO, base);
    this.moverJogo(jogo, GameStatus.ENCERRADO);
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
      case GameStatus.ENCERRADO:
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
