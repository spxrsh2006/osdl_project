package com.hotel;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    // ----------------------------------------------------------------
    //  DATA MODELS
    // ----------------------------------------------------------------

    public static class Room {
        private final StringProperty roomNumber;
        private final StringProperty type;
        private final DoubleProperty  price;
        private final StringProperty status;        // "Available" | "Occupied"
        private final StringProperty customerName;  // empty when free

        public Room(String roomNumber, String type, double price) {
            this.roomNumber   = new SimpleStringProperty(roomNumber);
            this.type         = new SimpleStringProperty(type);
            this.price        = new SimpleDoubleProperty(price);
            this.status       = new SimpleStringProperty("Available");
            this.customerName = new SimpleStringProperty("");
        }

        public StringProperty roomNumberProperty()   { return roomNumber;   }
        public StringProperty typeProperty()          { return type;         }
        public DoubleProperty  priceProperty()        { return price;        }
        public StringProperty statusProperty()        { return status;       }
        public StringProperty customerNameProperty()  { return customerName; }

        public String getRoomNumber()    { return roomNumber.get();   }
        public String getType()          { return type.get();         }
        public double getPrice()         { return price.get();        }
        public String getStatus()        { return status.get();       }
        public String getCustomerName()  { return customerName.get(); }
    }

    public static class Customer {
        private final StringProperty name;
        private final StringProperty contact;

        public Customer(String name, String contact) {
            this.name    = new SimpleStringProperty(name);
            this.contact = new SimpleStringProperty(contact);
        }

        public StringProperty nameProperty()    { return name;    }
        public StringProperty contactProperty() { return contact; }

        public String getName()    { return name.get();    }
        public String getContact() { return contact.get(); }
    }

    // ----------------------------------------------------------------
    //  APPLICATION STATE
    // ----------------------------------------------------------------

    private final ObservableList<Room>     rooms     = FXCollections.observableArrayList(
        room -> new javafx.beans.Observable[]{ room.statusProperty(), room.customerNameProperty() }
    );
    private final ObservableList<Customer> customers = FXCollections.observableArrayList();

    // ----------------------------------------------------------------
    //  ENTRY POINT
    // ----------------------------------------------------------------

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hotel Management System");

        // ---- Sidebar ----
        VBox sidebar = buildSidebar();

        // ---- Content panels ----
        VBox roomsPanel    = buildRoomsPanel();
        VBox customersPanel = buildCustomersPanel();

        // Default view
        final VBox[] current = {roomsPanel};

        // Get sidebar buttons to wire navigation
        Button btnRooms     = (Button) sidebar.lookup("#navRooms");
        Button btnCustomers = (Button) sidebar.lookup("#navCustomers");

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(roomsPanel);

        btnRooms.setOnAction(e -> {
            btnRooms.getStyleClass().setAll("sidebar-button", "sidebar-button-active");
            btnCustomers.getStyleClass().setAll("sidebar-button");
            root.setCenter(roomsPanel);
        });

        btnCustomers.setOnAction(e -> {
            btnCustomers.getStyleClass().setAll("sidebar-button", "sidebar-button-active");
            btnRooms.getStyleClass().setAll("sidebar-button");
            root.setCenter(customersPanel);
        });

        // Mark Rooms as active by default
        btnRooms.getStyleClass().add("sidebar-button-active");

        Scene scene = new Scene(root, 1050, 680);
        scene.getStylesheets().add(
            getClass().getResource("/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    // ----------------------------------------------------------------
    //  SIDEBAR
    // ----------------------------------------------------------------

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");

        Label appName = new Label("HotelMS");
        appName.getStyleClass().add("sidebar-title");

        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 0, 8, 0));

        Label navHeader = new Label("NAVIGATION");
        navHeader.getStyleClass().add("sidebar-divider");

        Button btnRooms = new Button("  Rooms");
        btnRooms.setId("navRooms");
        btnRooms.getStyleClass().add("sidebar-button");
        btnRooms.setMaxWidth(Double.MAX_VALUE);

        Button btnCustomers = new Button("  Customers");
        btnCustomers.setId("navCustomers");
        btnCustomers.getStyleClass().add("sidebar-button");
        btnCustomers.setMaxWidth(Double.MAX_VALUE);

        sidebar.getChildren().addAll(appName, sep, navHeader, btnRooms, btnCustomers);
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        return sidebar;
    }

    // ----------------------------------------------------------------
    //  ROOMS PANEL
    // ----------------------------------------------------------------

    private VBox buildRoomsPanel() {

        // ---- Header ----
        Label title = new Label("Room Management");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("Add, book, and manage hotel rooms");
        subtitle.getStyleClass().add("label-subtitle");
        VBox titleBox = new VBox(2, title, subtitle);

        HBox header = new HBox(titleBox);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        // ---- Stats row ----
        HBox statsRow = buildStatsRow();

        // ---- Add-Room Form Card ----
        VBox addRoomCard = buildAddRoomCard(statsRow);

        // ---- Booking Card ----
        VBox bookingCard = buildBookingCard();

        // ---- Table ----
        TableView<Room> table = buildRoomTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox content = new VBox(14, statsRow, addRoomCard, bookingCard, table);
        content.setPadding(new Insets(18));
        VBox.setVgrow(content, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox panel = new VBox(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return panel;
    }

    // ---- Stats Row ----

    private HBox buildStatsRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label totalNum   = new Label("0");
        Label availNum   = new Label("0");
        Label occupiedNum = new Label("0");

        totalNum.getStyleClass().add("stat-number");
        availNum.getStyleClass().add("stat-number");
        occupiedNum.getStyleClass().add("stat-number");

        availNum.setStyle("-fx-text-fill: #10b981;");
        occupiedNum.setStyle("-fx-text-fill: #ef4444;");

        Label totalLbl   = new Label("Total Rooms");
        Label availLbl   = new Label("Available");
        Label occupiedLbl = new Label("Occupied");
        totalLbl.getStyleClass().add("stat-label");
        availLbl.getStyleClass().add("stat-label");
        occupiedLbl.getStyleClass().add("stat-label");

        VBox totalCard   = makeStatCard(totalNum,    totalLbl);
        VBox availCard   = makeStatCard(availNum,    availLbl);
        VBox occupiedCard = makeStatCard(occupiedNum, occupiedLbl);

        row.getChildren().addAll(totalCard, availCard, occupiedCard);

        // Update stats whenever rooms change
        rooms.addListener((ListChangeListener<Room>) change -> {
            totalNum.setText(String.valueOf(rooms.size()));
            long avail    = rooms.stream().filter(r -> "Available".equals(r.getStatus())).count();
            long occupied = rooms.stream().filter(r -> "Occupied".equals(r.getStatus())).count();
            availNum.setText(String.valueOf(avail));
            occupiedNum.setText(String.valueOf(occupied));
        });
        // Also update when room status changes (property listener added per-room in booking)

        return row;
    }

    private VBox makeStatCard(Label number, Label label) {
        VBox card = new VBox(2, number, label);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    // ---- Add Room Card ----

    private VBox buildAddRoomCard(HBox statsRow) {
        Label cardTitle = new Label("Add New Room");
        cardTitle.getStyleClass().add("card-title");

        // Form fields
        TextField roomNumField = new TextField();
        roomNumField.setPromptText("e.g. 101");
        roomNumField.setPrefWidth(130);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Single", "Double", "Deluxe");
        typeBox.setPromptText("Type");
        typeBox.setPrefWidth(130);

        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 1500");
        priceField.setPrefWidth(130);

        Button addBtn = new Button("Add Room");
        addBtn.getStyleClass().add("button-primary");

        addBtn.setOnAction(e -> {
            String num   = roomNumField.getText().trim();
            String type  = typeBox.getValue();
            String prStr = priceField.getText().trim();

            if (num.isEmpty() || type == null || prStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Please fill in all fields (Room Number, Type, Price).");
                return;
            }

            // Check duplicate room number
            boolean exists = rooms.stream()
                    .anyMatch(r -> r.getRoomNumber().equalsIgnoreCase(num));
            if (exists) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Room",
                        "Room number '" + num + "' already exists.");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(prStr);
                if (price <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Price",
                        "Please enter a valid positive price.");
                return;
            }

            rooms.add(new Room(num, type, price));
            roomNumField.clear();
            typeBox.setValue(null);
            priceField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Room " + num + " added successfully.");
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);

        form.add(makeFormLabel("Room Number:"), 0, 0);
        form.add(roomNumField, 1, 0);
        form.add(makeFormLabel("Room Type:"), 2, 0);
        form.add(typeBox, 3, 0);
        form.add(makeFormLabel("Price (₹/night):"), 4, 0);
        form.add(priceField, 5, 0);
        form.add(addBtn, 6, 0);
        GridPane.setValignment(addBtn, VPos.BOTTOM);

        VBox card = new VBox(10, cardTitle, form);
        card.getStyleClass().add("card");
        return card;
    }

    // ---- Booking Card ----

    private VBox buildBookingCard() {
        Label cardTitle = new Label("Book / Checkout Room");
        cardTitle.getStyleClass().add("card-title");

        // Room picker — backed by a SEPARATE copy so setAll won't corrupt the master list
        ObservableList<Room> roomPickerItems = FXCollections.observableArrayList(rooms);
        rooms.addListener((ListChangeListener<Room>) c -> {
            roomPickerItems.setAll(rooms);
        });

        ComboBox<Room> roomPicker = new ComboBox<>(roomPickerItems);
        roomPicker.setPromptText("Select Room");
        roomPicker.setPrefWidth(170);
        roomPicker.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null
                        : r.getRoomNumber() + "  [" + r.getStatus() + "]");
            }
        });
        roomPicker.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null
                        : r.getRoomNumber() + "  [" + r.getStatus() + "]");
            }
        });

        // Customer picker — select from already-registered customers
        ComboBox<Customer> custPicker = new ComboBox<>(customers);
        custPicker.setPromptText("Select Customer");
        custPicker.setPrefWidth(170);
        custPicker.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName() + " (" + c.getContact() + ")");
            }
        });
        custPicker.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName() + " (" + c.getContact() + ")");
            }
        });

        // Helper label shows selected customer's contact
        Label contactHint = new Label("");
        contactHint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        custPicker.setOnAction(e -> {
            Customer c = custPicker.getValue();
            contactHint.setText(c != null ? "Contact: " + c.getContact() : "");
        });

        Button bookBtn = new Button("Book Room");
        bookBtn.getStyleClass().add("button-success");

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("button-warning");

        // Book action
        bookBtn.setOnAction(e -> {
            Room selected   = roomPicker.getValue();
            Customer cust   = custPicker.getValue();

            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Room Selected",
                        "Please select a room to book.");
                return;
            }
            if ("Occupied".equals(selected.getStatus())) {
                showAlert(Alert.AlertType.ERROR, "Room Occupied",
                        "Room " + selected.getRoomNumber() + " is already occupied.");
                return;
            }
            if (cust == null) {
                showAlert(Alert.AlertType.WARNING, "No Customer Selected",
                        "Please select a customer from the dropdown.\n" +
                        "Register the customer first in the Customers panel.");
                return;
            }

            // Check if customer is already booked in another room
            boolean alreadyBooked = rooms.stream()
                    .anyMatch(r -> r.getCustomerName().equalsIgnoreCase(cust.getName()));
            if (alreadyBooked) {
                showAlert(Alert.AlertType.ERROR, "Customer Already Booked",
                        cust.getName() + " is already booked in another room. Checkout first.");
                return;
            }

            // Book the room
            selected.statusProperty().set("Occupied");
            selected.customerNameProperty().set(cust.getName());

            // Refresh the picker list so status labels update
            roomPickerItems.setAll(rooms);
            roomPicker.setValue(null);
            custPicker.setValue(null);
            contactHint.setText("");

            showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed",
                    "Room " + selected.getRoomNumber() + " booked for " + cust.getName() + ".");
        });

        // Checkout action
        checkoutBtn.setOnAction(e -> {
            Room selected = roomPicker.getValue();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Room Selected",
                        "Please select a room to checkout.");
                return;
            }
            if ("Available".equals(selected.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Room Not Booked",
                        "Room " + selected.getRoomNumber() + " is not currently occupied.");
                return;
            }

            String guestName = selected.getCustomerName();
            selected.statusProperty().set("Available");
            selected.customerNameProperty().set("");

            roomPickerItems.setAll(rooms);
            roomPicker.setValue(null);
            custPicker.setValue(null);
            contactHint.setText("");

            showAlert(Alert.AlertType.INFORMATION, "Checkout Complete",
                    "Guest '" + guestName + "' has checked out from Room "
                    + selected.getRoomNumber() + ".");
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(8);

        form.add(makeFormLabel("Room:"),     0, 0);
        form.add(roomPicker,                 1, 0);
        form.add(makeFormLabel("Customer:"), 2, 0);

        VBox custPickerBox = new VBox(3, custPicker, contactHint);
        form.add(custPickerBox, 3, 0);

        HBox buttons = new HBox(10, bookBtn, checkoutBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        form.add(buttons, 4, 0);
        GridPane.setValignment(buttons, VPos.CENTER);

        VBox card = new VBox(10, cardTitle, form);
        card.getStyleClass().add("card");
        return card;
    }

    // ---- Room Table ----

    @SuppressWarnings("unchecked")
    private TableView<Room> buildRoomTable() {
        TableView<Room> table = new TableView<>(rooms);
        table.setPlaceholder(new Label("No rooms added yet. Use the form above to add rooms."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(220);

        TableColumn<Room, String> colNum  = new TableColumn<>("Room No");
        colNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colNum.setMinWidth(90);

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setMinWidth(90);

        TableColumn<Room, Double> colPrice = new TableColumn<>("Price (₹/night)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setMinWidth(120);
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("₹ %.2f", val));
            }
        });

        TableColumn<Room, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setMinWidth(100);
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(val);
                    if ("Available".equals(val)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<Room, String> colCust = new TableColumn<>("Guest Name");
        colCust.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCust.setMinWidth(140);
        colCust.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null || val.isEmpty() ? "—" : val);
            }
        });

        // Delete action column
        TableColumn<Room, Void> colAction = new TableColumn<>("Action");
        colAction.setMinWidth(90);
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button del = new Button("Remove");
            { del.getStyleClass().add("button-danger");
              del.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;"); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(del);
                del.setOnAction(e -> {
                    Room r = getTableView().getItems().get(getIndex());
                    if ("Occupied".equals(r.getStatus())) {
                        showAlert(Alert.AlertType.ERROR, "Cannot Remove",
                                "Room " + r.getRoomNumber() + " is currently occupied. Checkout first.");
                        return;
                    }
                    rooms.remove(r);
                });
            }
        });

        table.getColumns().addAll(colNum, colType, colPrice, colStatus, colCust, colAction);
        return table;
    }

    // ----------------------------------------------------------------
    //  CUSTOMERS PANEL
    // ----------------------------------------------------------------

    private VBox buildCustomersPanel() {

        // ---- Header ----
        Label title = new Label("Customer Records");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("View all registered guests");
        subtitle.getStyleClass().add("label-subtitle");
        VBox titleBox = new VBox(2, title, subtitle);

        HBox header = new HBox(titleBox);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        // ---- Add Customer Card ----
        VBox addCustCard = buildAddCustomerCard();

        // ---- Customer Table ----
        TableView<Customer> table = buildCustomerTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(300);

        VBox content = new VBox(16, addCustCard, table);
        content.setPadding(new Insets(18));
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox panel = new VBox(header, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private VBox buildAddCustomerCard() {
        Label cardTitle = new Label("Register New Customer");
        cardTitle.getStyleClass().add("card-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setPrefWidth(200);

        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        contactField.setPrefWidth(180);

        Button addBtn = new Button("Register Customer");
        addBtn.getStyleClass().add("button-primary");

        addBtn.setOnAction(e -> {
            String name    = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (name.isEmpty() || contact.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Please enter both Name and Contact Number.");
                return;
            }
            boolean exists = customers.stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name));
            if (exists) {
                showAlert(Alert.AlertType.WARNING, "Duplicate",
                        "A customer with name '" + name + "' is already registered.");
                return;
            }
            customers.add(new Customer(name, contact));
            nameField.clear();
            contactField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Registered",
                    "Customer '" + name + "' registered successfully.");
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);

        form.add(makeFormLabel("Name:"),    0, 0);
        form.add(nameField,                 1, 0);
        form.add(makeFormLabel("Contact:"), 2, 0);
        form.add(contactField,              3, 0);
        form.add(addBtn,                    4, 0);
        GridPane.setValignment(addBtn, VPos.BOTTOM);

        VBox card = new VBox(10, cardTitle, form);
        card.getStyleClass().add("card");
        return card;
    }

    @SuppressWarnings("unchecked")
    private TableView<Customer> buildCustomerTable() {
        TableView<Customer> table = new TableView<>(customers);
        table.setPlaceholder(new Label("No customers registered yet."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Customer, String> colName = new TableColumn<>("Customer Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setMinWidth(200);

        TableColumn<Customer, String> colContact = new TableColumn<>("Contact Number");
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colContact.setMinWidth(160);

        // Linked room column — re-evaluates whenever any room's customerName/status changes
        TableColumn<Customer, String> colRoom = new TableColumn<>("Booked Room");
        colRoom.setMinWidth(120);
        colRoom.setCellValueFactory(cellData -> {
            Customer c = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> rooms.stream()
                        .filter(r -> r.getCustomerName().equalsIgnoreCase(c.getName()))
                        .map(Room::getRoomNumber)
                        .findFirst().orElse("—"),
                rooms  // rooms has extractor on statusProperty + customerNameProperty
            );
        });
        colRoom.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle("—".equals(v) ? "-fx-text-fill: #94a3b8;"
                                       : "-fx-text-fill: #10b981; -fx-font-weight: bold;");
            }
        });

        // Delete customer column
        TableColumn<Customer, Void> colDel = new TableColumn<>("Action");
        colDel.setMinWidth(100);
        colDel.setCellFactory(tc -> new TableCell<>() {
            private final Button del = new Button("Remove");
            { del.getStyleClass().add("button-danger");
              del.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;"); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(del);
                del.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    boolean isBooked = rooms.stream()
                            .anyMatch(r -> r.getCustomerName().equalsIgnoreCase(c.getName()));
                    if (isBooked) {
                        showAlert(Alert.AlertType.ERROR, "Cannot Remove",
                                "Customer '" + c.getName() + "' has an active booking. Checkout first.");
                        return;
                    }
                    customers.remove(c);
                });
            }
        });

        table.getColumns().addAll(colName, colContact, colRoom, colDel);
        return table;
    }

    // ----------------------------------------------------------------
    //  HELPERS
    // ----------------------------------------------------------------

    private Label makeFormLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        return l;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ----------------------------------------------------------------
    //  MAIN
    // ----------------------------------------------------------------

    public static void main(String[] args) {
        launch(args);
    }
}
