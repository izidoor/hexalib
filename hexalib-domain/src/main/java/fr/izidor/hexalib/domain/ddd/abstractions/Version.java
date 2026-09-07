package fr.izidor.hexalib.domain.ddd.abstractions;


public record Version(long value) {

    public Version {
        if (value < 1L) {
            throw new IllegalArgumentException("version must be strictly positive");
        }
    }

    public static Version initial() { return new Version(1L); }
    public Version next() { return new Version(value + 1); }

    public static Version with(long value) { return new Version(value); }


}
