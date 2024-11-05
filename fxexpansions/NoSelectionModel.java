package fxexpansions;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;


public class NoSelectionModel<E> extends MultipleSelectionModel<E> {

	@Override
	public ObservableList<Integer> getSelectedIndices() {
		return FXCollections.emptyObservableList();
	}

	@Override
	public ObservableList<E> getSelectedItems() {
		return FXCollections.emptyObservableList();
	}

	@Override
	public void selectIndices(int index, int... indices) {

	}

	@Override
	public void selectAll() {

	}

	@Override
	public void selectFirst() {

	}

	@Override
	public void selectLast() {

	}

	@Override
	public void clearAndSelect(int index) {

	}

	@Override
	public void select(int index) {
		//Logging.instance.log("Selected "+index);
	}

	@Override
	public void select(E obj) {
		//Logging.instance.log("Selected "+obj);
	}

	@Override
	public void clearSelection(int index) {

	}

	@Override
	public void clearSelection() {

	}

	@Override
	public boolean isSelected(int index) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public void selectPrevious() {
		//Logging.instance.log("Selected prev");
	}

	@Override
	public void selectNext() {
		//Logging.instance.log("Selected next");
	}

}
