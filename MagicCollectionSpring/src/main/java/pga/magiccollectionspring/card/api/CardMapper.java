package pga.magiccollectionspring.card.api;

import pga.magiccollectionspring.card.api.dto.CardDTO;
import pga.magiccollectionspring.card.api.dto.CardMetadataDTO;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.CardIndexView;
import pga.magiccollectionspring.card.domain.ColorMaskCodec;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.shared.abstractions.IMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CardMapper implements IMapper<Card, CardDTO> {

    @Override
    public CardDTO map(Card card) {
        if (card == null) return null;
        return new CardDTO(
                card.getScryfallId(),
                card.getName(),
                card.getPrintedName(),
                card.getSetCode(),
                card.getScryfallId(),
                card.getOracleText(),
                card.getTypeLine(),
                card.getManaCost(),
                card.getConvertedManaCost(),
                card.getCmc(),
                card.getRarity(),
                card.getRarityRank(),
                card.getColorMask(),
                card.getIdentityMask(),
                null,
                null,
                null,
                null,
                null
        );
    }

    public CardDTO map(CardScryfallDTO scryfallDto) {
        if (scryfallDto == null) return null;
        
        CardDTO.ImageUrisDTO imageUris = null;
        if (scryfallDto.getImageUris() != null) {
            imageUris = new CardDTO.ImageUrisDTO(
                    scryfallDto.getImageUris().getSmall(),
                    scryfallDto.getImageUris().getNormal(),
                    scryfallDto.getImageUris().getLarge(),
                    scryfallDto.getImageUris().getPng()
            );
        } else if (scryfallDto.getCardFaces() != null && !scryfallDto.getCardFaces().isEmpty()) {
            // Soporte para DFC (Double Faced Cards)
            CardScryfallDTO.CardFaceDTO firstFace = scryfallDto.getCardFaces().get(0);
            if (firstFace.getImageUris() != null) {
                imageUris = new CardDTO.ImageUrisDTO(
                        firstFace.getImageUris().getSmall(),
                        firstFace.getImageUris().getNormal(),
                        firstFace.getImageUris().getLarge(),
                        firstFace.getImageUris().getPng()
                );
            }
        }

        return new CardDTO(
                scryfallDto.getScryfallId(),
                scryfallDto.getName(),
                scryfallDto.getPrintedName(),
                scryfallDto.getSetCode(),
                scryfallDto.getScryfallId(),
                scryfallDto.getOracleText(),
                scryfallDto.getTypeLine(),
                scryfallDto.getManaCost(),
                scryfallDto.getCmc() != null ? (int) Math.floor(scryfallDto.getCmc()) : null,
                scryfallDto.getCmc() != null ? scryfallDto.getCmc().floatValue() : null,
                scryfallDto.getRarity(),
                ColorMaskCodec.rarityRank(scryfallDto.getRarity()),
                ColorMaskCodec.colorMask(scryfallDto.getColors(), scryfallDto.getManaCost()),
                ColorMaskCodec.toMask(scryfallDto.getColorIdentity()),
                scryfallDto.getColors(),
                scryfallDto.getColorIdentity(),
                scryfallDto.getPower(),
                scryfallDto.getToughness(),
                imageUris
        );
    }

    public CardMetadataDTO mapMetadata(CardIndexView card, String lang, String localizedNameOverride) {
        if (card == null) {
            return null;
        }

        String normalizedLang = (lang == null || lang.isBlank()) ? "en" : lang.trim().toLowerCase(Locale.ROOT);
        String localizedName;
        if ("en".equals(normalizedLang)) {
            localizedName = card.getName();
        } else if (localizedNameOverride != null && !localizedNameOverride.isBlank()) {
            localizedName = localizedNameOverride;
        } else {
            localizedName = card.getName();
        }

        int colorMask = card.getColorMask() != null ? card.getColorMask() : 0;
        int identityMask = card.getIdentityMask() != null ? card.getIdentityMask() : 0;
        int rarityRank = card.getRarityRank() != null ? card.getRarityRank() : 0;

        return new CardMetadataDTO(
                card.getScryfallId(),
                card.getName(),
                localizedName,
                colorMask,
                identityMask,
                card.getManaCost(),
                card.getCmc(),
                rarityRank,
                card.getTypeLine(),
                card.getSetCode(),
                card.getImageSmallUrl()
        );
    }
}
