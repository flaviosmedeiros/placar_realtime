export enum GameStatus {
  NAO_INICIADO = 'NAO_INICIADO',
  EM_ANDAMENTO = 'EM_ANDAMENTO',
  ENCERRADO = 'ENCERRADO'
}

export interface game {
  id: string;
  timeA: string;
  timeB: string;
  placarA: number;
  placarB: number;
  status: string;
  dataHoraInicioPartida: Date;
  dataHoraEncerramento: Date;
}

export interface logGame {
  id: string;
  descricao: string;
  operacao: string;
  minutoPartida: number;
  dataHora: Date;
}
