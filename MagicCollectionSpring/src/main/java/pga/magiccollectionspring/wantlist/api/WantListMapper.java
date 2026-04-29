package pga.magiccollectionspring.wantlist.api;

import pga.magiccollectionspring.wantlist.api.dto.WantListCardDTO;
import pga.magiccollectionspring.wantlist.api.dto.WantListDTO;
import pga.magiccollectionspring.wantlist.domain.WantList;
import pga.magiccollectionspring.wantlist.domain.WantListCard;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class WantListMapper {

    public WantListDTO toDto(WantList wantList) {
        if (wantList == null) return null;
        
        return new WantListDTO(
                wantList.getId(),
                wantList.getName(),
                wantList.getOwner().getId(),
                wantList.getCards().stream()
                        .map(this::toCardDto)
                        .collect(Collectors.toList())
        );
    }

    public WantListCardDTO toCardDto(WantListCard card) {
        if (card == null) return null;
        
        return new WantListCardDTO(
                card.getId(),
                card.getScryfallId(),
                card.getName(),
                card.getTypeLine(),
                card.getManaCost(),
                card.getImageUrl(),
                card.getQuantity(),
                card.getFoil(),
                card.getLanguage(),
                card.getCondition()
        );
    }
}
