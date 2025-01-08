package me.sialim.riseoflands.government;

import me.angeschossen.lands.api.role.Role;

import java.util.Set;

public class GovernmentOptions {
    public enum PowerType {
        AUTOCRACY, COUNCIL, DEMOCRACY
    }

    public enum LawCreationMethod {
        LEADER, COUNCIL, CITIZEN_VOTE
    }

    public enum EconomySystem {
        FREE_MARKET, STATE_CONTROLLED
    }

    private boolean citizenProposalsAllowed;
    private boolean courtSystem;
    private boolean classMobilityEnabled;
    private boolean slaveryAllowed;
    private boolean freedomOfReligion;
    private boolean theocraticLeaders;
    private boolean electionsEnabled;
    private boolean termLimitsEnabled;
    private boolean hereditaryLeadership;

    private int electionFrequencyDays;
    private int termLengthDays;
    private int councilSize;

    private Set<Role> votingRoles;

    private PowerType powerType;
    private LawCreationMethod lawCreationMethod;
    private EconomySystem economySystem;
    //private Religion religion;
}
