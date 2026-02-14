package br.com.solides.placar.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.solides.placar.entity.Jogo;
import br.com.solides.placar.shared.dto.JogoFilterDTO;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Repository para operações de persistência da entidade Jogo.
 * Implementa operações CRUD com suporte a filtros dinâmicos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class JogoRepository {

    @PersistenceContext(unitName = "placar-pu")
    private EntityManager entityManager;

    /**
     * Salva um novo jogo ou atualiza existente
     */
    @Transactional
    public Jogo save(Jogo jogo) {
        log.debug("Salvando jogo: {}", jogo);
        
        if (jogo.getId() == null) {
            entityManager.persist(jogo);
            entityManager.flush();
            log.info("Jogo criado com ID: {}", jogo.getId());
            return jogo;
        } else {
            Jogo merged = entityManager.merge(jogo);
            log.info("Jogo atualizado com ID: {}", merged.getId());
            return merged;
        }
    }

    /**
     * Busca jogo por ID
     */
    public Optional<Jogo> findById(Long id) {
        log.debug("Buscando jogo por ID: {}", id);
        
        if (id == null) {
            return Optional.empty();
        }
        
        Jogo jogo = entityManager.find(Jogo.class, id);
        return Optional.ofNullable(jogo);
    }

    /**
     * Lista todos os jogos
     */
    public List<Jogo> findAll() {
        log.debug("Listando todos os jogos");
        
        TypedQuery<Jogo> query = entityManager.createQuery(
            "SELECT j FROM Jogo j ORDER BY j.dataHoraPartida DESC, j.id DESC", Jogo.class);
        
        return query.getResultList();
    }

    /**
     * Lista jogos com filtros dinâmicos
     */
    public List<Jogo> findByFilter(JogoFilterDTO filter) {
        log.debug("Buscando jogos com filtros: {}", filter);
        
        if (filter == null || !filter.hasFilters()) {
            return findAll();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Jogo> cq = cb.createQuery(Jogo.class);
        Root<Jogo> root = cq.from(Jogo.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Filtro por ID
        if (filter.getId() != null) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }
        
        // Filtro por Time A (like case-insensitive)
        if (filter.getTimeA() != null && !filter.getTimeA().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("timeA")), 
                "%" + filter.getTimeA().toLowerCase() + "%"));
        }
        
        // Filtro por Time B (like case-insensitive)
        if (filter.getTimeB() != null && !filter.getTimeB().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("timeB")), 
                "%" + filter.getTimeB().toLowerCase() + "%"));
        }
        
        // Filtro por Placar A
        if (filter.getPlacarA() != null) {
            predicates.add(cb.equal(root.get("placarA"), filter.getPlacarA()));
        }
        
        // Filtro por Placar B
        if (filter.getPlacarB() != null) {
            predicates.add(cb.equal(root.get("placarB"), filter.getPlacarB()));
        }
        
        // Filtro por Status
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        
        // Filtro por período de data/hora da partida
        if (filter.getDataHoraPartidaInicio() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dataHoraPartida"), 
                filter.getDataHoraPartidaInicio()));
        }
        
        if (filter.getDataHoraPartidaFim() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dataHoraPartida"), 
                filter.getDataHoraPartidaFim()));
        }
        
        // Aplicar filtros
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        
        // Ordenação padrão
        cq.orderBy(cb.desc(root.get("dataHoraPartida")));
        
        TypedQuery<Jogo> query = entityManager.createQuery(cq);
        List<Jogo> result = query.getResultList();
        
        log.debug("Encontrados {} jogos com os filtros aplicados", result.size());
        return result;
    }

    /**
     * Remove jogo por ID
     */
    @Transactional
    public boolean deleteById(Long id) {
        log.debug("Removendo jogo por ID: {}", id);
        
        if (id == null) {
            return false;
        }

        Optional<Jogo> jogoOpt = findById(id);
        if (jogoOpt.isPresent()) {
            entityManager.remove(jogoOpt.get());
            log.info("Jogo removido com ID: {}", id);
            return true;
        }
        
        log.warn("Jogo não encontrado para remoção, ID: {}", id);
        return false;
    }

    /**
     * Verifica se existe jogo com o ID
     */
    public boolean existsById(Long id) {
        log.debug("Verificando existência de jogo por ID: {}", id);
        
        if (id == null) {
            return false;
        }

        Long count = entityManager.createQuery(
            "SELECT COUNT(j) FROM Jogo j WHERE j.id = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();
        
        return count > 0;
    }

    /**
     * Conta total de jogos
     */
    public long count() {
        return entityManager.createQuery("SELECT COUNT(j) FROM Jogo j", Long.class)
            .getSingleResult();
    }

    /**
     * Lista jogos por status
     */
    public List<Jogo> findByStatus(StatusJogo status) {
        log.debug("Buscando jogos por status: {}", status);
        
        if (status == null) {
            return findAll();
        }

        TypedQuery<Jogo> query = entityManager.createQuery(
            "SELECT j FROM Jogo j WHERE j.status = :status ORDER BY j.dataHoraPartida DESC", 
            Jogo.class);
        
        query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Força refresh da entidade do banco
     */
    public void refresh(Jogo jogo) {
        if (jogo != null && jogo.getId() != null) {
            entityManager.refresh(jogo);
        }
    }

    /**
     * Flush das mudanças pendentes
     */
    public void flush() {
        entityManager.flush();
    }
}