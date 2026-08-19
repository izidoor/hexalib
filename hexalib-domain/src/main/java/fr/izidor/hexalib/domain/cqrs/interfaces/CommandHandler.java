package fr.izidor.hexalib.domain.cqrs.interfaces;

import fr.izidor.hexalib.domain.cqrs.commandResult.CommandResult;

public interface CommandHandler<C extends Command>{

    Class<C> listenToCommand();

    CommandResult<?> handle(C command);


}
