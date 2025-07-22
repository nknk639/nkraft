package com.nkraft.budget.service;

import com.nkraft.budget.dto.AccountDTO;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByNkraftUser_Id(userId);
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
        return accountRepository.findByNkraftUser_IdAndIsSavingsTrue(userId);
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

    private AccountDTO mapToDTO(Account entity) {
        return new AccountDTO(entity.getAccountId(), entity.getAccountName(), entity.getIsSavings(), entity.getIsMain());
    }
}