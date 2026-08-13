package fr.izidor.hexalib.domain.cqrs.interfaces;



public interface CommandHandler<C extends Command>{

    Class<C> listenToCommand();

    CommandResult handle(C command);


}
