package pga.magiccollectionspring.card.api.dto;

public record CardMetadataDTO(
        String scryfallId,
        String defaultName,
        String localizedName,
        Integer colorMask,
        Integer identityMask,
        String manaCost,
        Float cmc,
        Integer rarityRank,
        String typeLine,
        String setCode,
        String imageUrl
) {
}
