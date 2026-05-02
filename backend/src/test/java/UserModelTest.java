import org.example.exceptions.IllegalStatusTransitionException;
import org.example.exceptions.MaxWalletsExceededException;
import org.example.user.User;
import org.junit.Test;


import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UserModelTest {
    @Test
    public void userCreatesWallet() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");
        assertDoesNotThrow(guy::openWallet);
    }

    @Test
    public void throwWhenWalletsExceeded() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");
        for(int i = 0; i < 5; i++) {
            guy.openWallet();
        }

        assertThrows(MaxWalletsExceededException.class, guy::openWallet);
    }

    @Test
    public void verifyUnverifiedUser() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");

        assertDoesNotThrow(guy::verify);
    }

    @Test
    public void lockUnverifiedUser() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");
        assertDoesNotThrow(guy::lock);
    }

    @Test
    public void verifyLockedUser() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");
        guy.lock();

        assertDoesNotThrow(guy::verify);

    }

    @Test
    public void lockActiveUser() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");
        guy.verify();

        assertDoesNotThrow(guy::lock);
    }

    @Test
    public void throwWhenLockingLockedUser() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");
        guy.lock();

        assertThrows(IllegalStatusTransitionException.class, guy::lock);
    }

    @Test
    public void throwWhenVerifyingActiveUser() {
        User guy = new User("feef", "feef@feef.feef", "ksaugdyfeio2quy34rg5");
        guy.verify();

        assertThrows(IllegalStatusTransitionException.class, guy::verify);
    }
}
