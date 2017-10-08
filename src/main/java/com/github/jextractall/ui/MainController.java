package com.github.jextractall.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;

import com.github.jextractall.exceptions.InvalidArchiveException;
import com.github.jextractall.ui.i18n.Messages;
import com.github.jextractall.ui.model.ConfigModel;
import com.github.jextractall.ui.model.ConfigModelFactory;
import com.github.jextractall.ui.model.ExtractorTask;
import com.github.jextractall.ui.model.ExtractorTaskFactory;
import com.github.jextractall.ui.model.FilenameFilter;
import com.github.jextractall.ui.view.StatusTableCell;
import com.github.jextractall.unpack.ExtractionResult;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainController implements Initializable {

	@FXML
	TableColumn<ExtractorTask, ExtractionResult> statusColumn;
	@FXML
	TableView<ExtractorTask> taskView;
	@FXML
	TableColumn<ExtractorTask, String> fileColumn;
	@FXML
	TableColumn<ExtractorTask, String> statusTextColumn;
	@FXML
	TableColumn<ExtractorTask, Double> progressColumn;
	@FXML
	MenuItem mnScanNow;
	@FXML
	MenuItem mnDelete;
	@FXML
	Button startButton;
	@FXML
	MenuItem mnClear;
	@FXML
	TextField filterText;
	@FXML
	MenuItem mnDeleteFile;
	@FXML
	MenuItem mnToggleFilter;
	@FXML
	BorderPane borderPanel;
	@FXML
	BorderPane rootPane;
	@FXML
	Spinner<Integer> threadSpinner;

	public static final DirectoryChooser DIRECTORY_CHOOSER = new DirectoryChooser();

	private SimpleBooleanProperty showFilter = new SimpleBooleanProperty(true);

	private ObservableList<ExtractorTask> taskList = FXCollections.observableArrayList();
	private FilteredList<ExtractorTask> filteredTask = new FilteredList<>(taskList, new FilenameFilter());

	private TaskManager taskManager = new TaskManager(taskList);
	private static Stage stage;
	private ConfigModel configModel;

	@FXML
	public void onExtract() {
		if (taskManager.isRunning()) {
			taskManager.stopTasks();
		} else {
			taskManager.runTasks(configModel);
		}
	}

	@FXML
	public void onScanNow() {
		DIRECTORY_CHOOSER.setTitle(Messages.getMessage("search.title"));
		File dir = DIRECTORY_CHOOSER.showDialog(stage);
		if (dir == null) {
			return;
		}
		new Thread(() -> {
			try {
				ExtractorTaskFactory.scanForfiles(Paths.get(dir.getAbsolutePath()), taskList,
						configModel.getScannerModel().convertFileTypesToGlob(),
						configModel.getScannerModel().getGlobToIgnore());
			} catch (IOException ex) {
				DialogBuilder.exception(ex).withHeader(Messages.getMessage("error.search")).show();
			}
		}).start();
	}

	@FXML
	public void onDragDropped(DragEvent event) {
		if (event.getDragboard().hasFiles()) {
			for (File f : event.getDragboard().getFiles()) {
				addFilesToTaskList(f);
			}
			event.consume();
		}
	}

	public void addFilesToTaskList(File f) {
		if (f.isDirectory()) {
			try {
				ExtractorTaskFactory.scanForfiles(f.toPath(), taskList,
						configModel.getScannerModel().convertFileTypesToGlob(),
						configModel.getScannerModel().getGlobToIgnore());
			} catch (IOException e) {
			}
		} else {
			try {
				taskList.addAll(ExtractorTaskFactory.createFromPath(f.toPath()));
			} catch (InstantiationException | IllegalAccessException | InvalidArchiveException e) {
			}
		}
	}

	@FXML
	public void onDragOver(DragEvent event) {
		if (event.getDragboard().hasFiles()) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	@FXML
	public void onClear() {
		taskManager.removeAllTasks();
	}

	@FXML
	public void onDeleteSelected() {
		List<ExtractorTask> selectedItems = taskView.getSelectionModel().getSelectedItems();
		if (selectedItems != null) {
			for (ExtractorTask task : selectedItems) {
				taskManager.removeTaskFromTaskList(Arrays.asList(task));
				if (!taskList.contains(task)) {
					try {
						Files.deleteIfExists(task.getArchive());
					} catch (IOException e) {
						DialogBuilder.exception(e).withMessage(Messages.getMessage("error.delete", task.getFileName()))
								.show();
					}
				}
			}
		}
		taskView.getSelectionModel().clearSelection();
	}

	@FXML
	public void onToggleFilter() {
		showFilter.set(!showFilter.get());
		borderPanel.setTop(showFilter.get() ? filterText : null);
	}

	@FXML
	public void onOpenConfigDialog() {
		try {
			ConfigDialog dialog = new ConfigDialog(stage, (ConfigModel) configModel.clone());
			dialog.showAndWait().ifPresent(response -> {
				this.configModel = response;
				saveConfig();
			});
		} catch (IOException ioe) {
			DialogBuilder.exception(ioe).show();
		}

	}

	@FXML
	private void onRemoveSelected() {
		List<ExtractorTask> selectedItems = taskView.getSelectionModel().getSelectedItems();
		if (selectedItems != null) {
			taskManager.removeTaskFromTaskList(selectedItems);
		}
		taskView.getSelectionModel().clearSelection();
	}

	public void saveConfig() {
	    try {
            ConfigModelFactory.save(configModel);
        } catch (ConfigurationException ce) {
            DialogBuilder.exception(ce).withHeader(Messages.getMessage("error.saveConfig")).show();
        }
	}
	
	private void loadConfigModel() {
		try {
			configModel = ConfigModelFactory.load();
		} catch (ConfigurationException e) {
			DialogBuilder.warn().withHeader(Messages.getMessage("error.loadConfigTitle"))
					.withMessage(Messages.getMessage("error.loadConfigMessage")).show();
		}
	}

	private void initializeFilterTextField() {
		filterText.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredTask.setPredicate(task -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (task.getFileName().toLowerCase().contains(lowerCaseFilter)) {
					return true; // Filter matches first name.
				}
				if (task.getStatus().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
				return false; // Does not match.
			});
		});
		filterText.visibleProperty().bind(showFilter);
		onToggleFilter();
	}

	private void initializeTaskView() {
		taskView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		taskView.setPlaceholder(new Label(Messages.getMessage("main.tableView.defaultLabel")));
		taskView.setItems(filteredTask);

		statusColumn.setCellValueFactory(new PropertyValueFactory<ExtractorTask, ExtractionResult>("result"));
		statusColumn.setCellFactory((tableColumn) -> new StatusTableCell());

		fileColumn.setCellValueFactory(new PropertyValueFactory<ExtractorTask, String>("fileName"));
		fileColumn.setCellFactory((tableColumn) -> new TextFieldTableCell<ExtractorTask, String>() {
			@Override
			public void updateItem(String string, boolean isEmpty) {
				super.updateItem(string, isEmpty);
				if (!isEmpty && getTableRow() != null && getTableRow().getItem() != null) {
					ExtractorTask task = (ExtractorTask) getTableRow().getItem();
					Tooltip tip = new Tooltip(task.getArchive().toString());
					setTooltip(tip);
				} else {
					setTooltip(null);
				}
			}
		});

		statusTextColumn.setCellValueFactory(new PropertyValueFactory<ExtractorTask, String>("status"));
		statusTextColumn.setCellFactory(TextFieldTableCell.forTableColumn());

		progressColumn.setCellValueFactory(new PropertyValueFactory<ExtractorTask, Double>("progress"));
		progressColumn.setCellFactory(ProgressBarTableCell.<ExtractorTask>forTableColumn());

		// align width of the file column with the view's width
		fileColumn.prefWidthProperty()
				.bind(taskView.widthProperty().subtract(statusColumn.getWidth() + progressColumn.getWidth() + 30.0));

		// Hack to remove the tableheader
		// taskView.widthProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> ov, Number t,
		// Number t1) {
		// // Get the table header
		// Pane header = (Pane)taskView.lookup("TableHeaderRow");
		// if(header!=null && header.isVisible()) {
		// header.setMaxHeight(0);
		// header.setMinHeight(0);
		// header.setPrefHeight(0);
		// header.setVisible(false);
		// header.setManaged(false);
		// }
		// taskView.widthProperty().removeListener(this);
		// }
		// });
	}

	private void initializeSpinner() {
		SpinnerValueFactory<Integer> f = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
				Runtime.getRuntime().availableProcessors());
		threadSpinner.setValueFactory(f);
	}

	private void setupBindings() {
		mnDelete.disableProperty().bind(taskView.getSelectionModel().selectedItemProperty().isNull());
		mnDeleteFile.disableProperty().bind(mnDelete.disableProperty());
		mnClear.disableProperty().bind(Bindings.size(taskList).isEqualTo(0));
		startButton.textProperty().bind(new StringBinding() {
			{
				super.bind(taskManager.runningProperty());
			}

			@Override
			protected String computeValue() {
				return Messages.getMessage(taskManager.isRunning() ? "main.stop.label" : "main.start.label");
			}
		});
		threadSpinner.disableProperty().bind(taskManager.runningProperty());
		threadSpinner.valueProperty().addListener((source, oldV, newV) -> taskManager.setNumProcesses(newV));
	}

	public void initialize(URL location, ResourceBundle resources) {
		loadConfigModel();
		initializeFilterTextField();
		initializeTaskView();
		initializeSpinner();
		setupBindings();

		taskManager.registerCallback(new TaskHandler());
	}

	public ConfigModel getConfigModel() {
		return configModel;
	}

	public void setConfigModel(ConfigModel model) {
		this.configModel = model;
	}

	public void setStage(Stage stage) {
		MainController.stage = stage;
	}

	public static Stage getStage() {
		return MainController.stage;
	}

	class TaskHandler implements TaskCallback {

		@Override
		public void onComplete(ExtractorTask task) {
			if (task.getConfig().getPostExtractionModel().getScanExtracted()) {
				task.getExtractedFiles().stream().forEach(p -> {
					try {
						ArrayList<ExtractorTask> newTasks = new ArrayList<>();
						ExtractorTaskFactory.scanForfiles(p, newTasks,
								task.getConfig().getScannerModel().convertFileTypesToGlob(),
								task.getConfig().getScannerModel().getGlobToIgnore());
						taskList.addAll(newTasks.stream().filter(ExtractorTask::isValid).collect(Collectors.toList()));
					} catch (Exception ex) {
					}
				});
				if (!taskManager.isRunning() && taskManager.hasQueuedTasks()) {
					taskManager.runTasks(task.getConfig());
				}
				task.getNewPassword().ifPresent(p -> configModel.getExtractorModel().addPassword(p));
			}
			closeIfApplicable();
		}

		@Override
		public void onCancelled(ExtractorTask task) {
			closeIfApplicable();
		}

		@Override
		public void onFailure(ExtractorTask task) {
			closeIfApplicable();
		}

		public void closeIfApplicable() {
			if (MainController.this.configModel.getPostExtractionModel().getCloseApplication()
					&& !taskManager.isRunning() && !taskManager.hasQueuedTasks()) {
				stage.close();
			}
		}
	}
}
