package br.com.solides.placar.wicket.components.modal;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import br.com.solides.placar.service.JogoService;
import br.com.solides.placar.shared.dto.JogoDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import br.com.solides.placar.util.DateTimeConstants;
import br.com.solides.placar.util.PublisherUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Modal para editar um jogo existente. Permite alterar todos os campos conforme
 * regras de negócio.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@Slf4j
public class EditarJogoModal extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private JogoService jogoService;

	private EditarJogoForm formData;
	private Form<EditarJogoForm> form;

	/**
	 * Callback executado após edição bem-sucedida
	 */
	private ModalCallback onSuccess;

	public interface ModalCallback extends Serializable {
		void onSuccess(AjaxRequestTarget target, JogoDTO jogo);

		void onError(AjaxRequestTarget target, String mensagem);

		void onCancel(AjaxRequestTarget target);
	}

	public EditarJogoModal(String id, ModalCallback callback) {
		super(id);
		this.onSuccess = callback;
		this.formData = new EditarJogoForm();

		setOutputMarkupId(true);
		setOutputMarkupPlaceholderTag(true);

		initializeComponents();
	}

	private void initializeComponents() {
		// Form principal
		form = new Form<>("formEditarJogo", new CompoundPropertyModel<>(formData));

		// Campos do formulário
		adicionarCampos();

		// Botões
		adicionarBotoes();

		add(form);
	}

	private void adicionarCampos() {
		// Time A
		RequiredTextField<String> campoTimeA = new RequiredTextField<>("timeA");
		campoTimeA.setLabel(Model.of("Time A"));
		form.add(campoTimeA);

		// Time B
		RequiredTextField<String> campoTimeB = new RequiredTextField<>("timeB");
		campoTimeB.setLabel(Model.of("Time B"));
		form.add(campoTimeB);

		// Placar A
		NumberTextField<Integer> campoPlacarA = new NumberTextField<>("placarA");
		campoPlacarA.setMinimum(0);
		campoPlacarA.setLabel(Model.of("Placar A"));
		form.add(campoPlacarA);

		// Placar B
		NumberTextField<Integer> campoPlacarB = new NumberTextField<>("placarB");
		campoPlacarB.setMinimum(0);
		campoPlacarB.setLabel(Model.of("Placar B"));
		form.add(campoPlacarB);

		// Status
		List<StatusJogo> statusOptions = Arrays.asList(StatusJogo.values());
		DropDownChoice<StatusJogo> campoStatus = new DropDownChoice<>("status", statusOptions);
		campoStatus.setRequired(true);
		form.add(campoStatus);

		// Data da Partida
		LocalDateTextField campoDataPartida = new LocalDateTextField("dataPartida", "yyyy-MM-dd");
		campoDataPartida.setLabel(Model.of("Data da Partida"));
		campoDataPartida.add(AttributeModifier.replace("type", "date"));
		campoDataPartida.setRequired(true);
		form.add(campoDataPartida);

		// Hora da Partida
		TextField<String> campoHoraPartida = new TextField<>("horaPartidaStr");
		campoHoraPartida.setLabel(Model.of("Hora da Partida"));
		campoHoraPartida.setRequired(true);
		campoHoraPartida.add(AttributeModifier.replace("type", "time"));
		form.add(campoHoraPartida);
	}

	private void adicionarBotoes() {
		// Botão Salvar
		AjaxButton botaoSalvar = new AjaxButton("botaoSalvar") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				try {
					log.info("Atualizando jogo ID: {}", formData.getId());

					// Converter form data para DTO
					JogoDTO jogoDTO = formData.toJogoDTO();

					// Atualizar jogo via service
					JogoDTO jogoAtualizado = jogoService.atualizarJogo(jogoDTO);

					// Callback de sucesso
					if (onSuccess != null) {
						onSuccess.onSuccess(target, jogoAtualizado);
					}

					log.info("Jogo atualizado com sucesso: ID {}", jogoAtualizado.getId());

				} catch (Exception e) {
					log.error("Erro ao atualizar jogo", e);

					if (onSuccess != null) {
						onSuccess.onError(target, "Erro ao atualizar jogo: " + e.getMessage());
					}
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				log.warn("Erro de validação no form de editar jogo");

				if (onSuccess != null) {
					onSuccess.onError(target, "Verifique os dados informados");
				}
			}
		};
		form.add(botaoSalvar);

		// Botão Cancelar
		AjaxButton botaoCancelar = new AjaxButton("botaoCancelar") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				log.debug("Cancelando edição de jogo");

				if (onSuccess != null) {
					onSuccess.onCancel(target);
				}
			}
		};
		botaoCancelar.setDefaultFormProcessing(false);
		form.add(botaoCancelar);
	}

	/**
	 * Carrega dados do jogo no formulário
	 */
	public void carregarJogo(JogoDTO jogo) {
		formData = EditarJogoForm.fromJogoDTO(jogo);
		form.setModelObject(formData);
	}

	/**
	 * Classe interna para dados do formulário
	 */
	public static class EditarJogoForm implements Serializable {
		private static final long serialVersionUID = 1L;

		// ...existing code...
		private Long id;
		private String timeA;
		private String timeB;
		private Integer placarA;
		private Integer placarB;
		private StatusJogo status;
		private LocalDate dataPartida;
		private String horaPartidaStr;
		private LocalDateTime dataCriacao;
		private LocalDateTime dataAtualizacao;

		public EditarJogoForm() {
		}

		public static EditarJogoForm fromJogoDTO(JogoDTO jogo) {
			EditarJogoForm form = new EditarJogoForm();
			form.id = jogo.getId();
			form.timeA = jogo.getTimeA();
			form.timeB = jogo.getTimeB();
			form.placarA = jogo.getPlacarA();
			form.placarB = jogo.getPlacarB();
			form.status = jogo.getStatus();
			form.dataCriacao = jogo.getDataCriacao();
			form.dataAtualizacao = jogo.getDataAtualizacao();

			form.dataPartida = jogo.getDataPartida();
			form.horaPartidaStr = jogo.getHoraPartida();

			return form;
		}

		public JogoDTO toJogoDTO() {
			try {

				return JogoDTO.builder().id(id).timeA(timeA).timeB(timeB).placarA(placarA).placarB(placarB)
						.status(status).dataPartida(dataPartida).horaPartida(horaPartidaStr)
						.tempoDeJogo(gerarTempoDeJogo(this)).dataCriacao(dataCriacao).dataAtualizacao(dataAtualizacao)
						.build();
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Formato de data ou hora inválido. Use YYYY-MM-DD para data e HH:MM para hora.", e);
			}
		}

		private Integer gerarTempoDeJogo(EditarJogoForm entity) {
			Integer tempoDeJogo = 0;

			// Construir LocalDateTime a partir dos campos de data e hora
			if (!PublisherUtils.nuloOuVazio(entity.getDataPartida())
					&& !PublisherUtils.nuloOuVazio(entity.getHoraPartidaStr())) {
				try {
					LocalDateTime dataHoraPartida = LocalDateTime.of(entity.getDataPartida(),
							java.time.LocalTime.parse(entity.getHoraPartidaStr(), DateTimeConstants.TIME_FORMAT));

					if (entity.getStatus() == StatusJogo.EM_ANDAMENTO) {
						LocalDateTime agora = LocalDateTime.now();
						tempoDeJogo = (int) java.time.Duration.between(dataHoraPartida, agora).toMinutes();
					} else if (entity.getStatus() == StatusJogo.FINALIZADO) {
						// Para jogos finalizados, usar dataAtualizacao como aproximação do encerramento
						// ou calcular um tempo padrão de 90 minutos se não houver dataAtualizacao
						LocalDateTime dataEncerramento = entity.getDataAtualizacao() != null
								? entity.getDataAtualizacao()
								: dataHoraPartida.plusMinutes(90); // Tempo padrão de jogo
						tempoDeJogo = (int) java.time.Duration.between(dataHoraPartida, dataEncerramento).toMinutes();
					}
				} catch (Exception e) {
					// Em caso de erro na conversão, retorna 0
					tempoDeJogo = 0;
				}
			}

			return tempoDeJogo;
		}

		// Getters e Setters
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getTimeA() {
			return timeA;
		}

		public void setTimeA(String timeA) {
			this.timeA = timeA;
		}

		public String getTimeB() {
			return timeB;
		}

		public void setTimeB(String timeB) {
			this.timeB = timeB;
		}

		public Integer getPlacarA() {
			return placarA;
		}

		public void setPlacarA(Integer placarA) {
			this.placarA = placarA;
		}

		public Integer getPlacarB() {
			return placarB;
		}

		public void setPlacarB(Integer placarB) {
			this.placarB = placarB;
		}

		public StatusJogo getStatus() {
			return status;
		}

		public void setStatus(StatusJogo status) {
			this.status = status;
		}

		public LocalDate getDataPartida() {
			return dataPartida;
		}

		public void setDataPartida(LocalDate dataPartidaStr) {
			this.dataPartida = dataPartidaStr;
		}

		public String getHoraPartidaStr() {
			return horaPartidaStr;
		}

		public void setHoraPartidaStr(String horaPartidaStr) {
			this.horaPartidaStr = horaPartidaStr;
		}

		public LocalDateTime getDataCriacao() {
			return dataCriacao;
		}

		public void setDataCriacao(LocalDateTime dataCriacao) {
			this.dataCriacao = dataCriacao;
		}

		public LocalDateTime getDataAtualizacao() {
			return dataAtualizacao;
		}

		public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
			this.dataAtualizacao = dataAtualizacao;
		}
	}
}