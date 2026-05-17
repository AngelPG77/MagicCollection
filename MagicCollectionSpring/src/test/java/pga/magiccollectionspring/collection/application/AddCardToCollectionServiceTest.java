package pga.magiccollectionspring.collection.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pga.magiccollectionspring.collection.application.command.AddCardToCollection.AddCardToCollectionCommand;
import pga.magiccollectionspring.collection.application.command.AddCardToCollection.AddCardToCollectionService;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.CollectionCard;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.application.IUserInternalService;
import pga.magiccollectionspring.user.domain.User;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCardToCollectionServiceTest {

    @Mock private ICollectionRepository collectionRepository;
    @Mock private IUserInternalService userInternalService;
    @Mock private CurrentUserProvider currentUserProvider;

    private AddCardToCollectionService service;

    private User owner;
    private Collection collection;

    @BeforeEach
    void setUp() {
        service = new AddCardToCollectionService(collectionRepository, userInternalService, currentUserProvider);

        owner = new User(1L, "alice", "hash");
        collection = new Collection("Test Deck", owner);
        collection.setId(10L);

        when(currentUserProvider.getCurrentUsername()).thenReturn("alice");
        when(userInternalService.findByUsername("alice")).thenReturn(Optional.of(owner));
        when(collectionRepository.findById(10L)).thenReturn(Optional.of(collection));
    }

    @Test
    void execute_addsNewCard_whenVariantDoesNotExist() {
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(cmd("abc", false, "en", "NEAR_MINT", 1));

        assertThat(collection.getCards()).hasSize(1);
        assertThat(collection.getCards().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void execute_mergesQuantity_whenExactSameVariant() {
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CollectionCard existing = new CollectionCard("abc", "Lightning Bolt", null, null, null, 2, false, "en", "NEAR_MINT");
        collection.addCard(existing);

        service.execute(cmd("abc", false, "en", "NEAR_MINT", 3));

        assertThat(collection.getCards()).hasSize(1);
        assertThat(collection.getCards().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void execute_addsNewCard_whenOnlyFoilDiffers() {
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        collection.addCard(new CollectionCard("abc", "Lightning Bolt", null, null, null, 1, false, "en", "NEAR_MINT"));

        service.execute(cmd("abc", true, "en", "NEAR_MINT", 1));

        assertThat(collection.getCards()).hasSize(2);
    }

    @Test
    void execute_addsNewCard_whenOnlyLanguageDiffers() {
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        collection.addCard(new CollectionCard("abc", "Lightning Bolt", null, null, null, 1, false, "en", "NEAR_MINT"));

        service.execute(cmd("abc", false, "es", "NEAR_MINT", 1));

        assertThat(collection.getCards()).hasSize(2);
    }

    @Test
    void execute_throwsUnauthorized_andNeverSaves_whenUserNotOwner() {
        User other = new User(99L, "alice", "hash");
        when(userInternalService.findByUsername("alice")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.execute(cmd("abc", false, "en", "NEAR_MINT", 1)))
                .isInstanceOf(UnauthorizedException.class);

        verify(collectionRepository, never()).save(any());
    }

    private AddCardToCollectionCommand cmd(String scryfallId, boolean foil, String lang, String cond, int qty) {
        return new AddCardToCollectionCommand(10L, scryfallId, "Lightning Bolt", null, null, null, qty, foil, lang, cond);
    }
}
