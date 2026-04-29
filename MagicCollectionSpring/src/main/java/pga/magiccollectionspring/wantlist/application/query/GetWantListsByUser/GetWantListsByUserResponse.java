package pga.magiccollectionspring.wantlist.application.query.GetWantListsByUser;

import pga.magiccollectionspring.wantlist.api.dto.WantListDTO;
import java.util.List;

public record GetWantListsByUserResponse(List<WantListDTO> wantLists) {}
