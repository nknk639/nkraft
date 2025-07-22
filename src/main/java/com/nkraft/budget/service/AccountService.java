package com.nkraft.budget.service;

import com.nkraft.budget.dto.AccountCreateDTO;
import com.nkraft.budget.dto.AccountUpdateDTO;
import com.nkraft.budget.dto.AccountDTO;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByNkraftUser_IdAndIsDeletedFalse(userId);
    }

    public List<AccountDTO> getAccountsByUserIdAsDTO(Long userId) {
        return getAccountsByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId));
    }

    public Optional<Account> findMainAccountByUserId(Long userId) {
        return accountRepository.findByNkraftUser_IdAndIsMainTrue(userId);
    }

    public List<Account> findSavingsAccountsByUserId(Long userId) {
        return accountRepository.findByNkraftUser_IdAndIsSavingsTrueAndIsDeletedFalse(userId);
    }

    /**
     * 貯金口座のリストをDTOとして取得します。
     * @param userId ユーザーID
     * @return 口座DTOのリスト
     */
    public List<AccountDTO> findSavingsAccountsByUserIdAsDTO(Long userId) {
        return findSavingsAccountsByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createAccount(AccountCreateDTO dto, com.nkraft.user.entity.NkraftUser user) {
        if (dto.isMain()) {
            unsetMainAccount(user);
        }

        Account account = new Account();
        account.setNkraftUser(user);
        account.setAccountName(dto.getAccountName());
        account.setBalance(dto.getInitialBalance());
        account.setIsMain(dto.isMain());
        account.setIsSavings(dto.isSavings());
        account.setIsDeleted(false);
        accountRepository.save(account);
    }

    @Transactional
    public void updateAccount(Long accountId, AccountUpdateDTO dto, com.nkraft.user.entity.NkraftUser user) {
        Account account = getAccountByIdAndUser(accountId, user);

        if (dto.isMain() && !account.getIsMain()) {
            unsetMainAccount(user);
        }

        account.setAccountName(dto.getAccountName());
        account.setIsMain(dto.isMain());
        account.setIsSavings(dto.isSavings());
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long accountId, com.nkraft.user.entity.NkraftUser user) {
        Account account = getAccountByIdAndUser(accountId, user);
        if (account.getIsMain()) {
            throw new IllegalStateException("メイン口座は削除できません。");
        }
        account.setIsDeleted(true);
        accountRepository.save(account);
    }

    private void unsetMainAccount(com.nkraft.user.entity.NkraftUser user) {
        accountRepository.findByNkraftUser_IdAndIsMainTrue(user.getId()).ifPresent(currentMain -> {
            currentMain.setIsMain(false);
            accountRepository.save(currentMain);
        });
    }

    private AccountDTO mapToDTO(Account entity) {
        return new AccountDTO(entity.getAccountId(), entity.getAccountName(), entity.getIsSavings(), entity.getIsMain());
    }

    private Account getAccountByIdAndUser(Long accountId, com.nkraft.user.entity.NkraftUser user) {
        Account account = getAccountById(accountId);
        if (!account.getNkraftUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission to access this account.");
        }
        return account;
    }
}