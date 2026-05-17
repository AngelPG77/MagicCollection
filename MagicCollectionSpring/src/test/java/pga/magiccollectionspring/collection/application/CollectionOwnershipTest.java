package pga.magiccollectionspring.collection.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pga.magiccollectionspring.collection.application.command.DeleteCollection.DeleteCollectionCommand;
import pga.magiccollectionspring.collection.application.command.DeleteCollection.DeleteCollectionService;
import pga.magiccollectionspring.collection.application.command.RemoveCardFromCollection.RemoveCardFromCollectionCommand;
import pga.magiccollectionspring.collection.application.command.RemoveCardFromCollection.RemoveCardFromCollectionService;
import pga.magiccollectionspring.collection.application.command.UpdateCollection.UpdateCollectionCommand;
import pga.magiccollectionspring.collection.application.command.UpdateCollection.UpdateCollectionService;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.CollectionCard;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.application.IUserInternalService;
import pga.magiccollectionspring.user.domain.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies that no collection service allows a non-owner to mutate another user's collection.
 * Each test uses currentUser = "eve" (id=99) against a collection owned by "alice" (id=1).
 */
@ExtendWith(MockitoExtension.class)
class CollectionOwnershipTest {

    @Mock private ICollectionRepository collectionRepository;
    @Mock private IUserInternalService userInternalService;
    @Mock private CurrentUserProvider currentUserProvider;

    private Collection aliceCollection;
    private User alice;
    private User eve;

    @BeforeEach
    void setUp() {
        alice = new User(1L, "alice", "hash");
        eve = new User(99L, "eve", "hash");

        aliceCollection = new Collection("Alice's Deck", alice);
        aliceCollection.setId(10L);

        CollectionCard card = new CollectionCard("abc", "Lightning Bolt", null, null, null, 1, false, "en", "NEAR_MINT");
        card.setId(100L);
        aliceCollection.addCard(card);

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(aliceCollection));
    }

    @Test
    void updateCollection_throwsUnauthorized_whenNotOwner() {
        when(currentUserProvider.getCurrentUsername()).thenReturn("eve");

        UpdateCollectionService service = new UpdateCollectionService(collectionRepository, currentUserProvider);

        assertThatThrownBy(() -> service.execute(new UpdateCollectionCommand(10L, "New Name")))
                .isInstanceOf(UnauthorizedException.class);

        verify(collectionRepository, never()).save(any());
    }

    @Test
    void deleteCollection_throwsUnauthorized_whenNotOwner() {
        when(currentUserProvider.getCurrentUsername()).thenReturn("eve");

        DeleteCollectionService service = new DeleteCollectionService(collectionRepository, currentUserProvider);

        assertThatThrownBy(() -> service.execute(new DeleteCollectionCommand(10L)))
                .isInstanceOf(UnauthorizedException.class);

        verify(collectionRepository, never()).deleteById(any());
    }

    @Test
    void removeCardFromCollection_throwsUnauthorized_whenNotOwner() {
        when(currentUserProvider.getCurrentUsername()).thenReturn("eve");
        when(userInternalService.findByUsername("eve")).thenReturn(Optional.of(eve));

        RemoveCardFromCollectionService service =
                new RemoveCardFromCollectionService(collectionRepository, userInternalService, currentUserProvider);

        assertThatThrownBy(() -> service.execute(new RemoveCardFromCollectionCommand(10L, 100L)))
                .isInstanceOf(UnauthorizedException.class);

        verify(collectionRepository, never()).save(any());
    }

    @Test
    void addCardToCollection_throwsUnauthorized_whenNotOwner() {
        when(currentUserProvider.getCurrentUsername()).thenReturn("eve");
        when(userInternalService.findByUsername("eve")).thenReturn(Optional.of(eve));

        var addService = new pga.magiccollectionspring.collection.application.command.AddCardToCollection.AddCardToCollectionService(
                collectionRepository, userInternalService, currentUserProvider);

        assertThatThrownBy(() -> addService.execute(
                new pga.magiccollectionspring.collection.application.command.AddCardToCollection.AddCardToCollectionCommand(
                        10L, "abc", "Lightning Bolt", null, null, null, 1, false, "en", "NEAR_MINT")))
                .isInstanceOf(UnauthorizedException.class);

        verify(collectionRepository, never()).save(any());
    }
}
