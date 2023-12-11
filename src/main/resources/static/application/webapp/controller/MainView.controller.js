sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast"
], function (Controller, MessageToast) {
    "use strict";

    console.log('MainView.controller.js Running');

    return Controller.extend("application.webapp.controller.MainView", {
        onInit: function () {
            console.log('onInit Running');
            this.fetchReaders();
            this.fetchBooks();
        },

        onRegisterReader: function () {
            
            console.log('onRegisterReader Running');
            var readerName = this.getView().byId("readerName").getValue();
            var oModel = this.getView().getModel();
            $.ajax({
                type: "POST",
                url: "/library/register-reader",
                data: { name: readerName },
                dataType: "json",
                success: function(data) {
                    MessageToast.show("New Reader registered!");
                    oModel.getProperty("/Readers").push(data);
                    oModel.refresh(true);
                },
                error: function(err){
                    MessageToast.show(err.toString());
                }
            });
        },

        onAddBook: function () {
            var bookTitle = this.getView().byId("bookTitle").getValue();
            var bookAuthor = this.getView().byId("bookAuthor").getValue();
            var oModel = this.getView().getModel();
            $.ajax({
                type: "POST",
                url: "/library/add-book",
                data: { title: bookTitle, author: bookAuthor },
                dataType: "json",
                success: function(data) {
                    MessageToast.show("New Book added!");
                    oModel.getProperty("/Books").push(data);
                    oModel.refresh(true);
                },
                error: function(err){
                    MessageToast.show(err.toString());
                }
            });
        },

        onSearchBooks: function (searchEvent) {
            var searchQuery = searchEvent.getParameter("query");
            var selectedIndex = this.getView().byId("searchType").getSelectedIndex();
            var searchType = selectedIndex === 0 ? "title" : "author";
            
            var oModel = this.getView().getModel();
            $.ajax({
                type: "GET",
                url: `/library/search-books?query=${searchQuery}&type=${searchType}`,
                dataType: "json",
                success: function(data) {
                    oModel.setProperty("/SearchResults", data);
                },
                error: function(err){
                    MessageToast.show(err.toString());
                }
            });
        },

        onBorrowBook: function () {
            var readerName = this.getView().byId("readerName").getValue();
            var bookTitle = this.getView().byId("bookTitle").getValue();
            var oModel = this.getView().getModel();
            $.ajax({
                type: "POST",
                url: `/library/borrow-book?reader=${readerName}&title=${bookTitle}`,
                dataType: "json",
                success: function(data) {
                    MessageToast.show("Book borrowed successfully!");
                    // TODO: You might need to manually adjust the quantity of the book in your model
                    // Or you can re-fetch the data of the books and the reader
                    this.fetchBooks();
                    this.fetchReaders();
                }.bind(this),
                error: function(err){
                    MessageToast.show(err.toString());
                }
            });
        },

        onReturnBook: function () {
            var readerName = this.getView().byId("readerName").getValue();
            var bookTitle = this.getView().byId("bookTitle").getValue();
            var oModel = this.getView().getModel();
            $.ajax({
                type: "POST",
                url: `/library/return-book?reader=${readerName}&title=${bookTitle}`,
                dataType: "json",
                success: function(data) {
                    MessageToast.show("Book returned successfully!");
                    this.fetchBooks();
                    this.fetchReaders();
                }.bind(this),
                error: function(err){
                    MessageToast.show(err.toString());
                }
            });
        },

        onChangeLanguage: function (languageSelectEvent) {
            var selectedLanguage = languageSelectEvent.getParameter("selectedItem").getKey();
            // Here you need to implement the SAPUI5 language change according to https://sapui5.hana.ondemand.com/#/topic/8f93bf2a04ee4ca5b0b5a629bad55760.html
        },

        fetchReaders: function () {
            console.log('Fetch Readers Running');
            var oModel = this.getView().getModel();
            $.ajax({
                type: "GET",
                url: "/library/readers",
                dataType: "json",
                success: function(data) {
                    oModel.setProperty("/Readers", data);
                },
                error: function(err) {
                    MessageToast.show(err.toString());
                }
            });
        },

        fetchBooks: function () {
            console.log('Fetch Books Running');
            var oModel = this.getView().getModel();
            $.ajax({
                type: "GET",
                url: "/library/books",
                dataType: "json",
                success: function(data) {
                    oModel.setProperty("/Books", data);
                },
                error: function(err) {
                    MessageToast.show(err.toString());
                }
            });
        }
    });
});