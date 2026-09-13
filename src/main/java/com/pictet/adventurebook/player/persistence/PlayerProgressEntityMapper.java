package com.pictet.adventurebook.player.persistence;

import com.pictet.adventurebook.domain.PlayerProgress;
import org.springframework.stereotype.Component;

@Component
class PlayerProgressEntityMapper {

    PlayerProgressEntity toPlayerProgressEntity(PlayerProgress progress) {
        return new PlayerProgressEntity(progress.playerId(), progress.bookId(),
                progress.currentSectionId(), progress.health(), progress.status());
    }

    PlayerProgressEntity updatePlayerProgressEntity(PlayerProgressEntity entity, PlayerProgress progress) {
        entity.setCurrentSectionId(progress.currentSectionId());
        entity.setHealth(progress.health());
        entity.setStatus(progress.status());

        return entity;
    }

    PlayerProgress toPlayerProgressDomain(PlayerProgressEntity entity) {
        return new PlayerProgress(entity.getPlayerId(), entity.getBookId(),
                entity.getCurrentSectionId(), entity.getHealth(), entity.getStatus());
    }
}
