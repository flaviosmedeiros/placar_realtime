# Implementacao - Games Realtime (SSE)

## Visao geral
Esta implementacao consome eventos SSE de uma API REST Java para atualizar, em tempo real, tres listas de partidas de futebol:
- jogosNovos (status NAO_INICIADO)
- jogosEmAndamento (status EM_ANDAMENTO)
- jogosEncerrados (status ENCERRADO)

Cada partida aparece em apenas uma lista, de acordo com o status mais recente recebido.

## Modelos
Os modelos foram criados em `src/app/models/game.model.ts`:
- `GameStatus` (enum)
- `game` (interface da partida)
- `logGame` (interface de logs)

## SSE (4 endpoints)
A conexao SSE e feita pelo `EventSource` em `src/app/services/game-sse.service.ts`.
A configuracao fica concentrada nestas constantes:
- `apiBaseUrl`
- `sseEndpoints`

Endpoints esperados (ajuste conforme sua API Java):
- `GET {apiBaseUrl}/sse/jogos/novos`
- `GET {apiBaseUrl}/sse/jogos/inicio`
- `GET {apiBaseUrl}/sse/jogos/placar`
- `GET {apiBaseUrl}/sse/jogos/encerrado`

Formato esperado do payload (JSON enviado no SSE):
```
{
  "id": "uuid",
  "timeA": "Time A",
  "timeB": "Time B",
  "placarA": 0,
  "placarB": 0,
  "status": "NAO_INICIADO",
  "dataHoraInicioPartida": "2026-02-01T12:00:00",
  "dataHoraEncerramento": "2026-02-01T13:45:00"
}
```

Obs: Para atualizacao de placar, o payload pode ser parcial (ex: id, placarA, placarB). O front faz merge com os dados existentes.

## Regras de movimentacao
Implementadas em `src/app/app.ts`:
- Novo jogo: adiciona em `jogosNovos`.
- Inicio: remove de `jogosNovos` e adiciona em `jogosEmAndamento`.
- Placar: atualiza o item na lista `jogosEmAndamento`.
- Encerrado: remove de `jogosEmAndamento` e adiciona em `jogosEncerrados`.

## Logs no modal
Para jogos encerrados, ha um botao "Ver logs" que abre um modal e busca os logs via REST.
Endpoint esperado (ajuste conforme sua API):
- `GET {apiBaseUrl}/jogos/{id}/logs`

Formato esperado do log:
```
{
  "id": "uuid",
  "descricao": "Gol do time A",
  "operacao": "ATUALIZACAO_PLACAR",
  "minutoPartida": 32,
  "dataHora": "2026-02-01T12:32:00"
}
```

## Onde ajustar
- Base URL e endpoints SSE/Logs: `src/app/services/game-sse.service.ts`
- Tela principal e logica de listas: `src/app/app.ts`
- Layout: `src/app/app.html` e `src/app/app.css`

## Observacoes
- O `EventSource` faz reconexao automatica por padrao.
- Datas sao convertidas para `Date` e renderizadas via `Intl.DateTimeFormat`.
- Se a API enviar datas nulas, a tela mostra "-".
