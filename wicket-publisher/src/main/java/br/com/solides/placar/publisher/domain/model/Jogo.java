package br.com.solides.placar.publisher.domain.model;

import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidade JPA representando um jogo de futebol.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Entity
@Table(name = "jogos", schema = "placar")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jogo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Time A é obrigatório")
    @Column(name = "time_a", nullable = false, length = 100)
    private String timeA;

    @NotBlank(message = "Time B é obrigatório")
    @Column(name = "time_b", nullable = false, length = 100)
    private String timeB;

    @NotNull(message = "Placar A é obrigatório")
    @Min(value = 0, message = "Placar A não pode ser negativo")
    @Column(name = "placar_a", nullable = false)
    @Builder.Default
    private Integer placarA = 0;

    @NotNull(message = "Placar B é obrigatório")
    @Min(value = 0, message = "Placar B não pode ser negativo")
    @Column(name = "placar_b", nullable = false)
    @Builder.Default
    private Integer placarB = 0;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusJogo status = StatusJogo.EM_ANDAMENTO;

    @NotNull(message = "Data e hora da partida é obrigatória")
    @Column(name = "data_hora_partida", nullable = false)
    private LocalDateTime dataHoraPartida;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    /**
     * Verifica se o placar do jogo pode ser atualizado.
     * Regra: Não permite atualização se o jogo estiver FINALIZADO.
     * 
     * @return true se pode atualizar, false caso contrário
     */
    public boolean podeAtualizarPlacar() {
        return this.status == StatusJogo.EM_ANDAMENTO;
    }

    /**
     * Atualiza o placar do jogo.
     * Lança exceção se o jogo estiver encerrado.
     * 
     * @param novoPlacarA novo placar do time A
     * @param novoPlacarB novo placar do time B
     * @throws IllegalStateException se o jogo estiver encerrado
     */
    public void atualizarPlacar(Integer novoPlacarA, Integer novoPlacarB) {
        if (!podeAtualizarPlacar()) {
            throw new IllegalStateException("Não é possível atualizar o placar de um jogo encerrado");
        }
        
        if (novoPlacarA < 0 || novoPlacarB < 0) {
            throw new IllegalArgumentException("Placar não pode ser negativo");
        }
        
        this.placarA = novoPlacarA;
        this.placarB = novoPlacarB;
    }

    /**
     * Encerra o jogo, mudando seu status para FINALIZADO.
     * 
     * @throws IllegalStateException se o jogo já estiver encerrado
     */
    public void encerrar() {
        if (this.status == StatusJogo.FINALIZADO) {
            throw new IllegalStateException("O jogo já está encerrado");
        }
        this.status = StatusJogo.FINALIZADO;
    }

    /**
     * Verifica se o jogo está encerrado.
     * 
     * @return true se encerrado, false caso contrário
     */
    public boolean isEncerrado() {
        return this.status == StatusJogo.FINALIZADO;
    }

    /**
     * Verifica se o jogo está em andamento.
     * 
     * @return true se em andamento, false caso contrário
     */
    public boolean isEmAndamento() {
        return this.status == StatusJogo.EM_ANDAMENTO;
    }
}
