import { Injectable, NgZone } from '@angular/core';
import { game, logGame } from '../models/game.model';

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
  private readonly apiBaseUrl = 'http://localhost:8585';
  private readonly sseEndpoints = {
    novos: '/sse/jogos/novos',
    inicio: '/sse/jogos/inicio',
    placar: '/sse/jogos/placar',
    encerrado: '/sse/jogos/encerrado'
  };
  private readonly logsEndpoint = '/jogos/:id/logs';
  private sources: EventSource[] = [];

  constructor(private readonly zone: NgZone) {}

  connect(handlers: GameSseHandlers): void {
    this.close();

    this.sources = [
      this.createSource(this.sseEndpoints.novos, handlers.onNovo, handlers.onError),
      this.createSource(this.sseEndpoints.inicio, handlers.onInicio, handlers.onError),
      this.createSource(this.sseEndpoints.placar, handlers.onPlacar, handlers.onError),
      this.createSource(this.sseEndpoints.encerrado, handlers.onEncerrado, handlers.onError)
    ];
  }

  close(): void {
    this.sources.forEach((source) => source.close());
    this.sources = [];
  }

  async fetchLogs(gameId: string): Promise<logGame[]> {
    const url = `${this.apiBaseUrl}${this.logsEndpoint.replace(':id', gameId)}`;
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error('Nao foi possivel carregar os logs da partida.');
    }

    const payload = (await response.json()) as logGame[];
    return payload.map((log) => ({
      ...log,
      dataHora: this.parseDate(log.dataHora) ?? new Date('')
    }));
  }

  private createSource(
    endpoint: string,
    handler: (update: GameUpdate) => void,
    onError?: (event: Event) => void
  ): EventSource {
    const source = new EventSource(`${this.apiBaseUrl}${endpoint}`);

    source.onmessage = (event) => {
      const update = this.parseUpdate(event.data);
      if (!update) {
        return;
      }

      this.zone.run(() => handler(update));
    };

    source.onerror = (event) => {
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
