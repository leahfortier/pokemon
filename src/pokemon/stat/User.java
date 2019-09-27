package pokemon.stat;

public enum User {
    ATTACKING,
    DEFENDING,
    BOTH;

    // Includes stats which are not exclusively attacking stats like HP or Speed
    public boolean isAttacking() {
        return this != DEFENDING;
    }

    // Includes stats which are not exclusively defending stats like HP or Speed
    public boolean isDefending() {
        return this != ATTACKING;
    }
}
