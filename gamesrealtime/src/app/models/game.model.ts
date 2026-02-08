export enum GameStatus {
  NAO_INICIADO = 'NAO_INICIADO',
  EM_ANDAMENTO = 'EM_ANDAMENTO',
  FINALIZADO = 'FINALIZADO'
}

export interface game {
  id: string;
  timeA: string;
  timeB: string;
  placarA: number;
  placarB: number;
  status: string;
  tempoDeJogo: number;
  dataHoraInicioPartida: Date;
  dataHoraEncerramento: Date;
}
