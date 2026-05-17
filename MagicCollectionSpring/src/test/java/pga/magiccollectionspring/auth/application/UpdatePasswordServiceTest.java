package pga.magiccollectionspring.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pga.magiccollectionspring.auth.application.command.UpdatePassword.UpdatePasswordCommand;
import pga.magiccollectionspring.auth.application.command.UpdatePassword.UpdatePasswordResponse;
import pga.magiccollectionspring.auth.application.command.UpdatePassword.UpdatePasswordService;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordServiceTest {

    @Mock private IUserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UpdatePasswordService updatePasswordService;

    @BeforeEach
    void setUp() {
        updatePasswordService = new UpdatePasswordService(userRepository, passwordEncoder);
    }

    @Test
    void execute_updatesPassword_whenCurrentPasswordCorrect() {
        User user = new User(1L, "alice", "old-hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("new-hash");

        UpdatePasswordResponse response = updatePasswordService.execute(
                new UpdatePasswordCommand(1L, "oldPass", "newPass"));

        assertThat(response.success()).isTrue();
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("new-hash");
    }

    @Test
    void execute_throwsUnauthorized_andNeverSaves_whenCurrentPasswordWrong() {
        User user = new User(1L, "alice", "old-hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> updatePasswordService.execute(
                new UpdatePasswordCommand(1L, "wrongPass", "newPass")))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, never()).save(any());
    }
}
