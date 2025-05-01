package de.mherbst.funkplotter.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DualNumberTest {

    // Definiere eine Toleranz für Fließkommavergleiche
    private static final double DELTA = 1e-9;

    @Test
    @DisplayName ("Default constructor should initialize real and dual to 0.0")
    void testDefaultConstructor () {
        DualNumber dn = new DualNumber ( );
        assertEquals ( 0.0, dn.getReal ( ), DELTA, "Default real part should be 0.0" );
        assertEquals ( 0.0, dn.getDual ( ), DELTA, "Default dual part should be 0.0" );
    }

    @Test
    @DisplayName ("Parameterized constructor should initialize real and dual parts")
    void testParameterizedConstructor () {
        double realVal = 5.0;
        double dualVal = 2.0;
        DualNumber dn = new DualNumber ( realVal, dualVal );

        assertEquals ( realVal, dn.getReal ( ), DELTA, "Parameterized real part mismatch" );
        assertEquals ( dualVal, dn.getDual ( ), DELTA, "Parameterized dual part mismatch" );
    }

    @Test
    @DisplayName ("setReal should update the real part")
    void testSetReal () {
        DualNumber dn = new DualNumber ( 1.0, 2.0 );
        double newReal = 10.0;
        double originalDual = dn.getDual ( );

        dn.setReal ( newReal );

        assertEquals ( newReal, dn.getReal ( ), DELTA, "Real part should be updated by setReal" );
        assertEquals ( originalDual, dn.getDual ( ), DELTA, "Dual part should remain unchanged by setReal" );
    }

    @Test
    @DisplayName ("setDual should update the dual part")
    void testSetDual () {
        DualNumber dn = new DualNumber ( 1.0, 2.0 );
        double newDual = 20.0;
        double originalReal = dn.getReal ( );

        dn.setDual ( newDual );

        assertEquals ( newDual, dn.getDual ( ), DELTA, "Dual part should be updated by setDual" );
        assertEquals ( originalReal, dn.getReal ( ), DELTA, "Real part should remain unchanged by setDual" );
    }


    @Test
    @DisplayName ("Addition of two DualNumbers")
    void testAdd () {
        DualNumber dn1 = new DualNumber ( 2.0, 3.0 ); // a + bε = 2 + 3ε
        DualNumber dn2 = new DualNumber ( 4.0, 5.0 ); // c + dε = 4 + 5ε
        double dn1RealBefore = dn1.getReal ( ); // Zur Überprüfung der Unveränderlichkeit
        double dn1DualBefore = dn1.getDual ( );
        double dn2RealBefore = dn2.getReal ( );
        double dn2DualBefore = dn2.getDual ( );

        // Erwartet: (a+c) + (b+d)ε = (2+4) + (3+5)ε = 6 + 8ε
        double expectedReal = 6.0;
        double expectedDual = 8.0;

        DualNumber result = dn1.add ( dn2 );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Addition real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Addition dual part mismatch" );

        // Prüfe Unveränderlichkeit der Originalobjekte
        assertEquals ( dn1RealBefore, dn1.getReal ( ), DELTA, "Original object dn1 real part should not change after add" );
        assertEquals ( dn1DualBefore, dn1.getDual ( ), DELTA, "Original object dn1 dual part should not change after add" );
        assertEquals ( dn2RealBefore, dn2.getReal ( ), DELTA, "Original object dn2 real part should not change after add" );
        assertEquals ( dn2DualBefore, dn2.getDual ( ), DELTA, "Original object dn2 dual part should not change after add" );
    }

    @Test
    @DisplayName ("Multiplication of two DualNumbers")
    void testMultiply () {
        DualNumber dn1 = new DualNumber ( 2.0, 3.0 ); // a + bε = 2 + 3ε
        DualNumber dn2 = new DualNumber ( 4.0, 5.0 ); // c + dε = 4 + 5ε
        double dn1RealBefore = dn1.getReal ( );
        double dn1DualBefore = dn1.getDual ( );
        double dn2RealBefore = dn2.getReal ( );
        double dn2DualBefore = dn2.getDual ( );

        // Erwartet: (a*c) + (a*d + b*c)ε
        // (2*4) + (2*5 + 3*4)ε = 8 + (10 + 12)ε = 8 + 22ε
        double expectedReal = 8.0;
        double expectedDual = 22.0;

        DualNumber result = dn1.multiply ( dn2 );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Multiplication real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Multiplication dual part mismatch" );

        // Prüfe Unveränderlichkeit der Originalobjekte
        assertEquals ( dn1RealBefore, dn1.getReal ( ), DELTA, "Original object dn1 real part should not change after multiply" );
        assertEquals ( dn1DualBefore, dn1.getDual ( ), DELTA, "Original object dn1 dual part should not change after multiply" );
        assertEquals ( dn2RealBefore, dn2.getReal ( ), DELTA, "Original object dn2 real part should not change after multiply" );
        assertEquals ( dn2DualBefore, dn2.getDual ( ), DELTA, "Original object dn2 dual part should not change after multiply" );
    }

    @Test
    @DisplayName ("Exponential function exp(x)")
    void testExp () {
        double a = 2.0;
        double b = 3.0;
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: exp(a) + b*exp(a)ε
        double expectedReal = Math.exp ( a );
        double expectedDual = b * Math.exp ( a );

        DualNumber result = dn.exp ( );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Exp real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Exp dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after exp" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after exp" );
    }

    @Test
    @DisplayName ("Exponential of Reciprocal function exp(1/x)")
    void testExp_1_over_x () {
        double a = 2.0;
        double b = 1.0; // Verwende b=1 für direkte Ableitung
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: f(a) + b*f'(a)ε
        // f(x) = exp(1/x) => f'(x) = -exp(1/x) / x^2
        // Erwartet: exp(1/a) + 1.0 * (-exp(1/a) / a^2) * ε
        double expectedReal = Math.exp ( 1.0 / a );
        double expectedDual = -Math.exp ( 1.0 / a ) / ( a * a );

        DualNumber result = dn.exp_1_over_x ( );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "exp(1/x) real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "exp(1/x) dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after exp_1_over_x" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after exp_1_over_x" );
    }

    @Test
    @DisplayName ("exp(1/x) with zero real part should produce Infinity/NaN")
    void testExp_1_over_x_ZeroReal () {
        DualNumber dn = new DualNumber ( 0.0, 3.0 ); // 0 + 3ε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        DualNumber result = dn.exp_1_over_x ( );

        // Prüfe Ergebnis
        assertTrue ( Double.isInfinite ( result.getReal ( ) ) && result.getReal ( ) > 0, "Real part should be Positive Infinity" );
        assertTrue ( Double.isNaN ( result.getDual ( ) ), "Dual part should be NaN" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after exp_1_over_x (zero case)" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after exp_1_over_x (zero case)" );
    }


    @Test
    @DisplayName ("Sine function sin(x)")
    void testSin () {
        double a = Math.PI / 2.0;
        double b = 2.0;
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: sin(a) + b*cos(a)ε
        double expectedReal = Math.sin ( a ); // sin(PI/2) = 1
        double expectedDual = b * Math.cos ( a ); // 2*cos(PI/2) = 2*0 = 0

        DualNumber result = dn.sin ( );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Sin real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Sin dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after sin" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after sin" );
    }

    @Test
    @DisplayName ("Cosine function cos(x)")
    void testCos () {
        double a = Math.PI / 2.0;
        double b = 2.0;
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: cos(a) - b*sin(a)ε
        double expectedReal = Math.cos ( a ); // cos(PI/2) = 0
        double expectedDual = -b * Math.sin ( a ); // -2*sin(PI/2) = -2*1 = -2

        DualNumber result = dn.cos ( );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Cos real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Cos dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after cos" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after cos" );
    }

    @Test
    @DisplayName ("Square function x^2")
    void testSquare () {
        double a = 3.0;
        double b = 2.0;
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: a^2 + 2*a*b*ε
        double expectedReal = a * a; // 3*3 = 9
        double expectedDual = 2 * a * b; // 2*3*2 = 12

        DualNumber result = dn.square ( );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Square real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Square dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after square" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after square" );

        // Verifiziere auch gegen Multiplikation mit sich selbst
        DualNumber multiplyResult = dn.multiply ( dn );
        assertEquals ( multiplyResult.getReal ( ), result.getReal ( ), DELTA, "Square should match multiply(self) real part" );
        assertEquals ( multiplyResult.getDual ( ), result.getDual ( ), DELTA, "Square should match multiply(self) dual part" );
    }

    @Test
    @DisplayName ("Power function x^n with positive integer n")
    void testPowPositiveExponent () {
        double a = 2.0;
        double b = 3.0;
        int n = 3;
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: a^n + n*a^(n-1)*b*ε
        double expectedReal = Math.pow ( a, n ); // 2^3 = 8
        double expectedDual = n * Math.pow ( a, n - 1 ) * b; // 3 * 2^(3-1) * 3 = 3 * 2^2 * 3 = 3 * 4 * 3 = 36

        DualNumber result = dn.pow ( n );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Pow(n>0) real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Pow(n>0) dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after pow" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after pow" );
    }

    @Test
    @DisplayName ("Power function x^n with n=0")
    void testPowZeroExponent () {
        double a = 2.0;
        double b = 3.0;
        int n = 0;
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: a^0 + 0*a^(-1)*b*ε = 1 + 0ε
        double expectedReal = 1.0;
        double expectedDual = 0.0;

        DualNumber result = dn.pow ( n );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Pow(0) real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Pow(0) dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after pow(0)" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after pow(0)" );
    }

    @Test
    @DisplayName ("Power function x^n with n=1")
    void testPowOneExponent () {
        double a = 2.0;
        double b = 3.0;
        int n = 1;
        DualNumber dn = new DualNumber ( a, b ); // a + bε
        double dnRealBefore = dn.getReal ( );
        double dnDualBefore = dn.getDual ( );

        // Erwartet: a^1 + 1*a^(0)*b*ε = a + bε
        double expectedReal = a;
        double expectedDual = b;

        DualNumber result = dn.pow ( n );

        // Prüfe Ergebnis
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Pow(1) real part mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Pow(1) dual part mismatch" );

        // Prüfe Unveränderlichkeit
        assertEquals ( dnRealBefore, dn.getReal ( ), DELTA, "Original object real part should not change after pow(1)" );
        assertEquals ( dnDualBefore, dn.getDual ( ), DELTA, "Original object dual part should not change after pow(1)" );
    }

    @Test
    @DisplayName ("toString() format")
    void testToString () {
        // Füge zuerst eine toString() Methode zu DualNumber hinzu:
        // @Override
        // public String toString() {
        //     return String.format("%f + %f*ε", real, dual);
        // }

        DualNumber dn = new DualNumber ( 1.23, -4.56 );
        // %f verwendet standardmäßig 6 Dezimalstellen
        String expected = String.format ( "{ real=%f, dual=ε * %f* }", 1.23, -4.56 );
        assertEquals ( expected, dn.toString ( ) );
    }


    // Optional: Test für die Beispielfunktion aus den Kommentaren
    // Definiere die Funktion f(x) = sin(x) * x^2 als statische Methode im Test oder in DualNumber
    private static DualNumber exampleFunctionF ( DualNumber x ) {
        // Verwendet die Instanzmethoden von DualNumber
        return x.sin ( ).multiply ( x.square ( ) );
        // Alternative: return x.sin().multiply(x.pow(2)); // pow(2) sollte das gleiche Ergebnis wie square() liefern
    }

    @Test
    @DisplayName ("Example function f(x) = sin(x) * x^2")
    void testExampleFunctionF () {
        double x0 = 2.0;
        DualNumber x = new DualNumber ( x0, 1.0 ); // Input x=2, Ableitung d/dx (dual=1)

        // Berechne erwartete Werte manuell
        // f(x) = sin(x) * x^2
        // f(2) = sin(2) * 2^2 = sin(2) * 4
        double expectedReal = Math.sin ( x0 ) * Math.pow ( x0, 2 );

        // f'(x) = d/dx [sin(x) * x^2] = cos(x)*x^2 + sin(x)*2x (Produktregel)
        // f'(2) = cos(2)*2^2 + sin(2)*2*2 = 4*cos(2) + 4*sin(2)
        double expectedDual = Math.cos ( x0 ) * Math.pow ( x0, 2 ) + Math.sin ( x0 ) * 2 * x0;

        // Führe die Funktion mit DualNumbers aus
        DualNumber result = exampleFunctionF ( x );

        // Assert
        assertEquals ( expectedReal, result.getReal ( ), DELTA, "Example function f(x) real part (function value) mismatch" );
        assertEquals ( expectedDual, result.getDual ( ), DELTA, "Example function f(x) dual part (derivative value) mismatch" );

        // Hinweis: Die manuell berechneten Werte (ca. 3.637, 1.973) sind die korrekten
        // für f(x) = sin(x) * x^2 und f'(x) bei x=2.
        // Die Werte im Kommentar der DualNumber-Klasse (1.637, -0.225) scheinen falsch zu sein
        // oder beziehen sich auf eine andere Funktion. Dieser Test prüft die Implementierung
        // der `exampleFunctionF` basierend auf den Methoden von DualNumber.
        // System.out.println("Expected f(2) = " + expectedReal); // ~3.637188
        // System.out.println("Expected f'(2) = " + expectedDual); // ~1.972600
        // System.out.println("Actual f(2) = " + result.getReal());
        // System.out.println("Actual f'(2) = " + result.getDual());
    }
}