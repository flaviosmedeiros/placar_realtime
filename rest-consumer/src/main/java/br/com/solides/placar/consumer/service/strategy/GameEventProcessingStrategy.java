package br.com.solides.placar.consumer.service.strategy;

import br.com.solides.placar.shared.event.PlacarAtualizadoEvent;

/**
 * Strategy interface for processing different types of game events.
 */
public interface GameEventProcessingStrategy {
    
    /**
     * Process the given game event.
     * 
     * @param event the game event to process
     */
    void process(PlacarAtualizadoEvent event);
    
    /**
     * Determines if this strategy can handle the given event.
     * 
     * @param event the game event to check
     * @return true if this strategy can handle the event, false otherwise
     */
    boolean canHandle(PlacarAtualizadoEvent event);
}