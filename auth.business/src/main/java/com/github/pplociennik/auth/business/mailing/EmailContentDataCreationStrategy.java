package com.github.pplociennik.auth.business.mailing;

import com.github.pplociennik.auth.business.mailing.domain.model.AddressableDataDO;
import com.github.pplociennik.auth.business.mailing.domain.model.EmailConfirmationDataDO;
import org.springframework.lang.NonNull;
import org.thymeleaf.context.Context;

import java.util.Optional;

import static com.github.pplociennik.auth.common.lang.AuthResEmailMsgTranslationKey.EMAIL_ACCOUNT_CONFIRMATION_DISCLAIMER;
import static com.github.pplociennik.auth.common.lang.AuthResEmailMsgTranslationKey.EMAIL_ACCOUNT_CONFIRMATION_MESSAGE;
import static com.github.pplociennik.util.utility.CustomObjects.arrayOf;
import static com.github.pplociennik.util.utility.LanguageUtil.getLocalizedMessage;
import static java.util.Objects.requireNonNull;

/**
 * A strategy for creating context data for email messages. Context data consists of the message's context and template file.
 *
 * @author Created by: Pplociennik at 30.06.2022 21:00
 */
enum EmailContentDataCreationStrategy {

    EMAIL_CONFIRMATION_MESSAGE {
        @Override
        EmailContentData prepare( @NonNull AddressableDataDO aDataDO ) {
            requireNonNull( aDataDO );

            final String templateFile = "confirmationRequestEmailTemplate";

            var emailData = getProperTypeOfDataDO( aDataDO, EmailConfirmationDataDO.class );

            var context = new Context();

            context.setVariable( "message", getLocalizedMessage( EMAIL_ACCOUNT_CONFIRMATION_MESSAGE ) );
            context.setVariable( "confirmationLink", emailData.getConfirmationLink() );
            context.setVariable( "disclaimer", getLocalizedMessage( EMAIL_ACCOUNT_CONFIRMATION_DISCLAIMER, arrayOf( 15 ) ) );

            return EmailContentData.of( context, templateFile );
        }

    };

    abstract EmailContentData prepare( @NonNull AddressableDataDO aDataDO );

    < T > T getProperTypeOfDataDO( @NonNull AddressableDataDO aDataDO, @NonNull Class< T > aType ) {
        requireNonNull( aDataDO );
        requireNonNull( aType );

        var checkedObject = Optional.of( aDataDO ).stream()
                .filter( aType::isInstance )
                .findAny()
                .orElseThrow( () -> new IllegalArgumentException( "Wrong type!" ) );
        return aType.cast( checkedObject );
    }
}