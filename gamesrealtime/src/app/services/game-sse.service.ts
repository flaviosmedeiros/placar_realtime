import { Injectable, NgZone } from '@angular/core';
import { game } from '../models/game.model';

type GameUpdate = Partial<game> & { id: string };

type GameSseHandlers = {
  onNovo: (update: GameUpdate) => void;
  onInicio: (update: GameUpdate) => void;
  onPlacar: (update: GameUpdate) => void;
  onEncerrado: (update: GameUpdate) => void;
  onError?: (event: Event) => void;
};

@Injectable({
  providedIn: 'root'
})
export class GameSseService {
  private readonly apiBaseUrl = 'http://localhost:8585/consumer/api';
  private readonly sseEndpoints = {
    novos: '/sse/games/novos',
    inicio: '/sse/games/inicio',
    placar: '/sse/games/placar',
    encerrado: '/sse/games/encerrado'
  };
  private sources: EventSource[] = [];

  constructor(private readonly zone: NgZone) { }

  connect(handlers: GameSseHandlers): void {
    this.close();

    this.sources = [
      this.createSource('novos', this.sseEndpoints.novos, handlers.onNovo, handlers.onError),
      this.createSource('inicio', this.sseEndpoints.inicio, handlers.onInicio, handlers.onError),
      this.createSource('placar', this.sseEndpoints.placar, handlers.onPlacar, handlers.onError),
      this.createSource('encerrado', this.sseEndpoints.encerrado, handlers.onEncerrado, handlers.onError)
    ];
  }

  close(): void {
    this.sources.forEach((source) => source.close());
    this.sources = [];
  }

  private createSource(
    channel: string,
    endpoint: string,
    handler: (update: GameUpdate) => void,
    onError?: (event: Event) => void
  ): EventSource {
    const url = `${this.apiBaseUrl}${endpoint}`;
    const source = new EventSource(url);

    source.onopen = () => {
      console.info(`[SSE:${channel}] conectado em ${url}`);
    };

    // Use addEventListener to listen for named events matching the channel name
    source.addEventListener(channel, (event: MessageEvent) => {
      const update = this.parseUpdate(event.data);
      if (!update) {
        console.warn(`[SSE:${channel}] mensagem ignorada (payload invalido)`, event.data);
        return;
      }

      console.info(`[SSE:${channel}] evento recebido`, update);
      this.zone.run(() => handler(update));
    });

    source.onerror = (event) => {
      console.error(`[SSE:${channel}] erro na conexao`, event);
      if (onError) {
        this.zone.run(() => onError(event));
      }
    };

    return source;
  }

  private parseUpdate(raw: string): GameUpdate | null {
    try {
      const parsed = JSON.parse(raw) as GameUpdate;
      return this.normalizeUpdate(parsed);
    } catch (error) {
      console.error('Mensagem SSE invalida', error);
      return null;
    }
  }

  private normalizeUpdate(update: GameUpdate): GameUpdate {
    return {
      ...update,
      dataHoraInicioPartida: this.parseDate(update.dataHoraInicioPartida),
      dataHoraEncerramento: this.parseDate(update.dataHoraEncerramento)
    };
  }

  private parseDate(value: unknown): Date | undefined {
    if (!value) {
      return undefined;
    }

    if (value instanceof Date) {
      return value;
    }

    const parsed = new Date(String(value));
    return parsed;
  }
}
