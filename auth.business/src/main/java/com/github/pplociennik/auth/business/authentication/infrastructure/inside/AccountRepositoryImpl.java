/*
 * MIT License
 *
 * Copyright (c) 2021 Przemysław Płóciennik
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.pplociennik.auth.business.authentication.infrastructure.inside;

import com.github.pplociennik.auth.business.authentication.domain.map.AccountMapper;
import com.github.pplociennik.auth.business.authentication.domain.model.AccountDO;
import com.github.pplociennik.auth.business.authentication.ports.inside.AccountRepository;
import com.github.pplociennik.auth.common.exc.AccountConfirmationException;
import com.github.pplociennik.auth.db.entity.authentication.Account;
import com.github.pplociennik.auth.db.repository.authentication.AccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import static com.github.pplociennik.auth.business.authentication.domain.map.AccountMapper.mapToDomain;
import static com.github.pplociennik.auth.business.authentication.domain.map.AccountMapper.mapToEntity;
import static com.github.pplociennik.auth.common.lang.AuthResExcMsgTranslationKey.ACCOUNT_CONFIRMATION_USER_NOT_EXISTS;
import static com.github.pplociennik.commons.utility.CustomCollectors.toSingleton;
import static java.util.Objects.requireNonNull;

/**
 * {@inheritDoc}
 *
 * @author Created by: Pplociennik at 28.11.2021 15:30
 */
class AccountRepositoryImpl implements AccountRepository {

    private final AccountDao accountDao;

    @Autowired
    public AccountRepositoryImpl( @NonNull AccountDao aAccountDao ) {
        accountDao = aAccountDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountDO persist( @NonNull AccountDO aAccountDO ) {
        requireNonNull( aAccountDO );

        var account = mapToEntity( aAccountDO );
        return mapToDomain( accountDao.saveAndFlush( account ) );
    }

    @Override
    public AccountDO update( @NonNull AccountDO aAccount ) {
        requireNonNull( aAccount );

        var toBeUpdated = accountDao.getAccountByUniqueObjectIdentifier( aAccount.getUniqueObjectIdentifier() );
        return toBeUpdated
                .map( forUpdate -> updateAccount( forUpdate, aAccount ) )
                .stream()
                .collect( toSingleton() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountDO findAccountByUsername( @NonNull String aUsername ) {
        requireNonNull( aUsername );
        var account = accountDao.findAccountByUsername( aUsername );
        return account
                .map( AccountMapper::mapToDomain )
                .orElse( null );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsAccountByUsername( @NonNull String aUsername ) {
        requireNonNull( aUsername );
        return accountDao.existsAccountByUsername( aUsername );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsAccountByEmailAddress( @NonNull String aEmail ) {
        requireNonNull( aEmail );
        return accountDao.existsAccountByEmailAddress( aEmail );
    }

    /**
     * Updates the specified account.
     *
     * @param aAccount
     *         an account to be updated.
     */
    @Override
    public AccountDO enableAccount( @NonNull AccountDO aAccount ) {
        requireNonNull( aAccount );

        var account = accountDao.findAccountByEmailAddress( aAccount.getEmailAddress() );
        var toUpdate = account.orElseThrow(
                () -> new AccountConfirmationException( ACCOUNT_CONFIRMATION_USER_NOT_EXISTS ) );
        toUpdate.setEnabled( true );

        var enabledAccount = accountDao.save( toUpdate );
        return mapToDomain( enabledAccount );
    }

    private AccountDO updateAccount( Account aToBeUpdated, AccountDO aAccount ) {

        requireNonNull( aToBeUpdated );

        aToBeUpdated.setLastLoginDate( aAccount.getLastLoginDate() );
        accountDao.saveAndFlush( aToBeUpdated );

        return mapToDomain( aToBeUpdated );
    }
}