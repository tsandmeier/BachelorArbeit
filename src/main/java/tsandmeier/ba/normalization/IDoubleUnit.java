package tsandmeier.ba.normalization;

public interface IDoubleUnit extends IUnit {

	public IDoubleUnitType getType();

	public double getNumeratorFactor();

	public double getDeterminatorFactor();

}
