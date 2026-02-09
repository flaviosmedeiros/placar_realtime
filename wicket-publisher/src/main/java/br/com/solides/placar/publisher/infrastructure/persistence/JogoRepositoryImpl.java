package br.com.solides.placar.publisher.infrastructure.persistence;

import br.com.solides.placar.publisher.domain.model.Jogo;
import br.com.solides.placar.publisher.domain.repository.JogoRepository;
import br.com.solides.placar.shared.enums.StatusJogo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Implementação JPA do repositório de Jogos.
 * 
 * @author Copilot
 * @since 1.0.0
 */
@ApplicationScoped
@Slf4j
public class JogoRepositoryImpl implements JogoRepository {

    @PersistenceContext(unitName = "placar-pu")
    private EntityManager entityManager;

    @Override
    public Jogo save(Jogo jogo) {
        if (jogo.getId() == null) {
            log.debug("Persistindo novo jogo: {}", jogo);
            entityManager.persist(jogo);
            return jogo;
        } else {
            log.debug("Atualizando jogo ID: {}", jogo.getId());
            return entityManager.merge(jogo);
        }
    }

    @Override
    public Optional<Jogo> findById(Long id) {
        log.debug("Buscando jogo por ID: {}", id);
        Jogo jogo = entityManager.find(Jogo.class, id);
        return Optional.ofNullable(jogo);
    }

    @Override
    public List<Jogo> findAll() {
        log.debug("Buscando todos os jogos");
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Jogo> cq = cb.createQuery(Jogo.class);
        Root<Jogo> root = cq.from(Jogo.class);
        
        cq.select(root)
          .orderBy(cb.desc(root.get("dataHoraPartida")));
        
        TypedQuery<Jogo> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    @Override
    public List<Jogo> findByStatus(StatusJogo status) {
        log.debug("Buscando jogos por status: {}", status);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Jogo> cq = cb.createQuery(Jogo.class);
        Root<Jogo> root = cq.from(Jogo.class);
        
        cq.select(root)
          .where(cb.equal(root.get("status"), status))
          .orderBy(cb.desc(root.get("dataHoraPartida")));
        
        TypedQuery<Jogo> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Removendo jogo ID: {}", id);
        Jogo jogo = entityManager.find(Jogo.class, id);
        if (jogo != null) {
            entityManager.remove(jogo);
        }
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Verificando existência do jogo ID: {}", id);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Jogo> root = cq.from(Jogo.class);
        
        cq.select(cb.count(root))
          .where(cb.equal(root.get("id"), id));
        
        Long count = entityManager.createQuery(cq).getSingleResult();
        return count > 0;
    }

    @Override
    public long count() {
        log.debug("Contando total de jogos");
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Jogo> root = cq.from(Jogo.class);
        
        cq.select(cb.count(root));
        
        return entityManager.createQuery(cq).getSingleResult();
    }
}
