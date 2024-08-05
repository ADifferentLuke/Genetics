package net.lukemcomber.genetics.utilities.model;

import net.lukemcomber.genetics.Ecosystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimulationSessions {

    private final ConcurrentMap<String, Ecosystem> sessions;

    public SimulationSessions(){
       sessions = new ConcurrentHashMap<>();
    }

    public void add( final String sessionId, final Ecosystem ecosystem ){
        sessions.put(sessionId,ecosystem);
    }

    public Ecosystem get( final String sessionId ){
        return sessions.getOrDefault(sessionId, null);
    }

    public Set<String> ids(){
        return sessions.keySet();
    }

    public Collection<Ecosystem> sessions(){
       return sessions.values();
    }
}
