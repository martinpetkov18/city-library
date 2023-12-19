sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast",
    "sap/ui/model/json/JSONModel"
], function (Controller, MessageToast, JSONModel) {
    "use strict";

    console.log('MainView.controller.js Running');

    return Controller.extend("application.webapp.controller.MainView", {
        onInit: function () {
            var oModel = new JSONModel({
                Readers: [],
                Books: [],
                SearchResults: []
            });
        
            this.getView().setModel(oModel);
            console.log('onInit Running');
            console.log("test 83");
        
            var that = this;
            this.fetchReaders().then(function(data) {
                var oModel2 = that.getView().getModel();
                oModel2.setProperty("/Readers", data);
                console.log(oModel2.getProperty("/Readers"));
            })
            .catch(function(err) {
                console.log(err);
             });
        
            this.fetchBooks().then(function(data) {
                var oModel3 = that.getView().getModel();
                oModel3.setProperty("/Books", data);
                console.log(oModel3.getProperty("/Books"));
            })
            .catch(function(err) {
                console.log(err);
            });
        },

        onRegisterReader: function () {
            var readerName = this.getView().byId("readerName").getValue();
            var oModel = this.getView().getModel();
            var that = this;
        
            $.ajax({
                type: "POST",
                url: "/library/register-reader",
                data: { name: readerName },

                success: function(data) {
                    MessageToast.show("New Reader registered!");
                    that.fetchReaders().then(function(data) {
                        var oModelReaders = that.getView().getModel();
                        oModelReaders.setProperty("/Readers", data);
                    })
                },
                error: function() {
                    MessageToast.show('An error occurred.');
                }
            });
        },

        onAddBook: function () {
            var bookTitle = this.getView().byId("bookTitle").getValue();
            var bookAuthor = this.getView().byId("bookAuthor").getValue();
            var oModel = this.getView().getModel();
            var that = this;

            $.ajax({
                type: "POST",
                url: "/library/add-book",
                data: { title: bookTitle, author: bookAuthor },

                success: function(data) {
                    MessageToast.show("New Book added!");
                    that.fetchBooks().then(function(data) {
                        var oModelBooks = that.getView().getModel();
                        oModelBooks.setProperty("/Books", data);
                    })
                },
                error: function() {
                    MessageToast.show('An error occurred.');
                }
            });
        },

        onSearchBooks: function (searchEvent) {
            var searchQuery = searchEvent.getParameter("query");
            var selectedIndex = this.getView().byId("searchType").getSelectedIndex();
            var searchType = selectedIndex === 0 ? "title" : "author";
            console.log("Search query: ", searchQuery);
            console.log("Search type: ", searchType);
            
            var oModel = this.getView().getModel();
            $.ajax({
                type: "GET",
                url: `/library/search-books?query=${searchQuery}&type=${searchType}`,
                dataType: "json",
                success: function(data) {
                    console.log("search");
                    oModel.setProperty("/SearchResults", data);
                    oModel.refresh(true);

                    if (data.length === 0) {
                        MessageToast.show("No books found!");
                    } else {
                        MessageToast.show("Search completed!");
                    }
                },
                error: function(err){
                    MessageToast.show(err.toString());
                }
            });
        },

        onBorrowBook: function () {
            var readerName = this.getView().byId("borrowerName").getValue();
            var bookTitle = this.getView().byId("borrowerBookTitle").getValue();
            var oModel = this.getView().getModel();
        
            $.ajax({
                type: "PUT",
                url: "/library/borrow-book",
                data: {readerName: readerName, bookTitle: bookTitle},
                dataType: "json",
                success: function(data) {
                    MessageToast.show("Book borrowed successfully!");
                    this.fetchBooks();
                    this.fetchReaders();
                }.bind(this),
                error: function(err){
                    MessageToast.show(err.toString());
                }
            });
        },

        onReturnBook: function () {
            var readerName = this.getView().byId("borrowerName").getValue();
            var bookTitle = this.getView().byId("borrowerBookTitle").getValue();
            var oModel = this.getView().getModel();

            $.ajax({
                type: "PUT",
                url: "/library/return-book",
                data: {readerName: readerName, bookTitle: bookTitle},
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
            var that = this;
        
            return new Promise(function(resolve, reject) {
                $.ajax({
                    type: "GET",
                    url: "/library/readers",
                    dataType: "json",
                    success: function(data) {
                        console.log(data);
                        resolve(data);
                    },
                    error: function(err) {
                        MessageToast.show(err.toString());
                        reject(err);
                    }
                });
            });
        },
        
        fetchBooks: function () {
            console.log('Fetch Books Running');
            var that = this;
        
            return new Promise(function(resolve, reject) {
                $.ajax({
                    type: "GET",
                    url: "/library/books",
                    dataType: "json",
                    success: function(data) {
                        console.log(data);
                        resolve(data);
                    },
                    error: function(err) {
                        MessageToast.show(err.toString());
                        reject(err);
                    }
                });
            });
        },
    });
});