package training.cd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import training.cd.infrastructure.Logger;
import training.cd.model.Account;
import training.cd.model.Person;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class BankTest {
  @Mock
  private Logger logger;

  private TestDatabase database;
  private Person personOne;
  private Bank bank;

  @Before
  public void setUp() throws Exception {
    database = new TestDatabase();
    bank = new Bank(database, logger);
    personOne = new Person(1, "Some User");
  }

  @Test(expected = RuntimeException.class)
  public void shouldNotCreateAccountWhenPersonIsNull() throws Exception {
    bank.createAccount(null);
  }

  @Test
  public void shouldCreateAccount() throws Exception {
    Account account = bank.createAccount(personOne);

    assertThat(account.owner.id, is(1L));
    assertThat(account.balance, is(0.0));
  }

  @Test
  public void shouldFetchAccountFromId() throws Exception {
    Account account = bank.createAccount(personOne);
    Account savedAccount = bank.account(account.id);

    assertThat(savedAccount.id, is(account.id));
  }

  @Test
  public void shouldDepositAmount() throws Exception {
    Account account = bank.createAccount(personOne);

    bank.deposit(10.0, account);

    assertThat(database.account(account.id).balance, is(10.0));
  }

  @Test
  public void shouldWithdrawAmount() throws Exception {
    Account account = bank.createAccount(personOne);
    bank.deposit(10.0, account);

    boolean status = bank.withdraw(10.0, account);

    assertThat(status, is(true));
    assertThat(database.account(account.id).balance, is(0.0));
  }

  @Test
  public void withdrawShouldFailIfBalanceIsNotEnough() throws Exception {
    Account account = bank.createAccount(personOne);

    boolean status = bank.withdraw(10.0, account);

    assertThat(status, is(false));
    assertThat(database.account(account.id).balance, is(0.0));
  }

  @Test
  public void shouldTransferAmount() throws Exception {
    Account from = bank.createAccount(personOne);
    Account to = bank.createAccount(new Person(2, "Some Other User"));
    bank.deposit(10.0, from);

    boolean status = bank.transfer(10.0, from, to);

    assertThat(status, is(true));
    assertThat(database.account(from.id).balance, is(0.0));
    assertThat(database.account(to.id).balance, is(10.0));
  }

  @Test
  public void shouldFailTransferWhenFromAccountDoesNotHaveEnoughBalance() throws Exception {
    Account from = bank.createAccount(personOne);
    Account to = bank.createAccount(new Person(2, "Some Other User"));

    boolean status = bank.transfer(10.0, from, to);

    assertThat(status, is(false));
    assertThat(database.account(from.id).balance, is(0.0));
    assertThat(database.account(to.id).balance, is(0.0));
  }

  @Test
  public void shouldFailTransferWhenToAccountIsInvalid() throws Exception {
    Account from = bank.createAccount(personOne);
    bank.deposit(10.0, from);

    boolean status = bank.transfer(10.0, from, null);

    assertThat(status, is(false));
    assertThat(database.account(from.id).balance, is(10.0));
  }
}