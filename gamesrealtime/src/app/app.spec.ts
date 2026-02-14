import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { GameSseService } from './services/game-sse.service';

type GameUpdate = {
  id: string | number;
  timeA?: string;
  timeB?: string;
  placarA?: number;
  placarB?: number;
  status?: string;
  tempoDeJogo?: number;
  dataHoraInicioPartida?: Date;
  dataHoraEncerramento?: Date;
};

type Handlers = {
  onNovo: (update: GameUpdate) => void;
  onInicio: (update: GameUpdate) => void;
  onPlacar: (update: GameUpdate) => void;
  onEncerrado: (update: GameUpdate) => void;
  onExcluido: (update: GameUpdate) => void;
  onError?: (event: Event) => void;
};

describe('App', () => {
  let handlers: Handlers | null = null;

  beforeEach(async () => {
    handlers = null;
    const sseMock: Pick<GameSseService, 'connect' | 'close'> = {
      connect: (incomingHandlers) => {
        handlers = incomingHandlers as Handlers;
      },
      close: () => undefined
    };

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: GameSseService, useValue: sseMock }]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Games Realtime');
  });

  it('should remove game from jogosNovos when excluido event arrives', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;

    handlers?.onNovo({ id: 'game-1', timeA: 'A', timeB: 'B' });
    expect(app['jogosNovos']().map((item) => item.id)).toContain('game-1');

    handlers?.onExcluido({ id: 'game-1' });
    expect(app['jogosNovos']().map((item) => item.id)).not.toContain('game-1');
  });

  it('should replace outdated game when receiving another "novos" event for the same id', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;

    const inicioOriginal = new Date('2026-02-13T10:00:00.000Z');
    const inicioAtualizado = new Date('2026-02-13T11:30:00.000Z');

    handlers?.onNovo({
      id: 10,
      timeA: 'Time A',
      timeB: 'Time B',
      dataHoraInicioPartida: inicioOriginal
    });

    handlers?.onNovo({
      id: '10',
      timeA: 'Time A',
      timeB: 'Time B',
      dataHoraInicioPartida: inicioAtualizado
    });

    const jogosNovos = app['jogosNovos']();
    expect(jogosNovos.length).toBe(1);
    expect(jogosNovos[0].id).toBe('10');
    expect(jogosNovos[0].dataHoraInicioPartida.getTime()).toBe(inicioAtualizado.getTime());
  });
});
