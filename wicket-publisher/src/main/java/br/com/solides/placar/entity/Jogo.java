package br.com.solides.placar.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA representando um Jogo de futebol.
 * Armazena informações sobre os times, placar e status da partida.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Entity(name = "Jogo")
@Table(name = "jogo", schema = "placar")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jogo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único do jogo
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Nome do time A
     */
    @NotBlank(message = "Time A é obrigatório")
    @Column(name = "time_a", nullable = false, length = 100)
    private String timeA;

    /**
     * Nome do time B
     */
    @NotBlank(message = "Time B é obrigatório")  
    @Column(name = "time_b", nullable = false, length = 100)
    private String timeB;

    /**
     * Placar do time A
     */
    @NotNull(message = "Placar A é obrigatório")
    @Min(value = 0, message = "Placar A não pode ser negativo")
    @Column(name = "placar_a", nullable = false)
    @Builder.Default
    private Integer placarA = 0;

    /**
     * Placar do time B
     */
    @NotNull(message = "Placar B é obrigatório")
    @Min(value = 0, message = "Placar B não pode ser negativo")
    @Column(name = "placar_b", nullable = false)
    @Builder.Default  
    private Integer placarB = 0;

    /**
     * Status atual do jogo
     */
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusJogo status = StatusJogo.NAO_INICIADO;

    /**
     * Data e hora da partida
     */
    @NotNull(message = "Data e hora da partida é obrigatória")
    @Column(name = "data_hora_partida", nullable = false)
    private LocalDateTime dataHoraPartida;

   
    /**
     * Data e hora de encerramento do jogo
     */
    @Column(name = "data_hora_encerramento")
    private LocalDateTime dataHoraEncerramento;
    
   
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

   
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    
    /**
     * Verifica se o jogo pode ter o placar alterado
     */
    public boolean podeAlterarPlacar() {
        return StatusJogo.EM_ANDAMENTO.equals(status);
    }

    /**
     * Verifica se o jogo pode ser inicializado
     */
    public boolean podeInicializar() {
        return StatusJogo.NAO_INICIADO.equals(status);
    }

    /**
     * Verifica se o jogo pode ser finalizado
     */
    public boolean podeFinalizar() {
        return StatusJogo.EM_ANDAMENTO.equals(status);
    }

    /**
     * Inicializa o jogo (muda status para EM_ANDAMENTO)
     */
    public void inicializar() {
        if (!podeInicializar()) {
            throw new IllegalStateException("Jogo não pode ser inicializado no status atual: " + status);
        }
        this.status = StatusJogo.EM_ANDAMENTO;
    }

    /**
     * Finaliza o jogo (muda status para FINALIZADO)
     */
    public void finalizar() {
        if (!podeFinalizar()) {
            throw new IllegalStateException("Jogo não pode ser finalizado no status atual: " + status);
        }
        this.status = StatusJogo.FINALIZADO;
        this.dataHoraEncerramento = LocalDateTime.now();
    }

    /**
     * Atualiza o placar do jogo
     */
    public void atualizarPlacar(Integer novoplacarA, Integer novoplacarB) {
        if (!podeAlterarPlacar()) {
            throw new IllegalStateException("Placar não pode ser alterado no status atual: " + status);
        }
        if (novoplacarA < 0 || novoplacarB < 0) {
            throw new IllegalArgumentException("Placares não podem ser negativos");
        }
        this.placarA = novoplacarA;
        this.placarB = novoplacarB;
    }
}