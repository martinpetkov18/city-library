sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast",
    "sap/ui/model/json/JSONModel"
], function (Controller, MessageToast, JSONModel) {
    "use strict";
    return Controller.extend("application.webapp.controller.MainView", {
        onInit: function () {
            const oModel = new JSONModel({
                Readers: [],
                Books: [],
                SearchResults: []
            });

            console.log("test 23");

            this.getView().setModel(oModel);

            this.fetchReaders().then(data => oModel.setProperty("/Readers", data)).catch(console.error);
            this.fetchBooks().then(data => oModel.setProperty("/Books", data)).catch(console.error);
            
            const oSelect = this.byId("languageSelect");
            oSelect.setSelectedKey(sap.ui.getCore().getConfiguration().getLanguage());

            this.getView().setModel(new sap.ui.model.resource.ResourceModel({
                bundleUrl: "/application/webapp/i18n/i18n.properties"
            }), "i18n");
        },

        onRegisterReader: function () {
            this.fetchResult({
                url: "/library/register-reader",
                type: "POST",
                data: { name: this.getView().byId("readerName").getValue() },
                successMessage: "New Reader registered!",
                updateMethod: this.fetchReaders,
                listProp: "/Readers",
                clearInputIds: ["readerName"]
            });
        },

        onShowBooks: function (oEvent) {
            var selectedKey = oEvent.getParameter("selectedItem").getKey();
            switch (selectedKey) {
                case 'all':
                    this.fetchBooks().then(data => this.getView().getModel().setProperty("/Books", data)).catch(console.error);
                    break;
                case 'available':
                    this.fetchAvailableBooks().then(data => this.getView().getModel().setProperty("/Books", data)).catch(console.error);
                    break;
                case 'reader':
                    var dialog = new sap.m.Dialog({
                        title: 'Enter the reader?s name:',
                        type: 'Message',
                        content: new sap.m.Input('submitDialogInput', {
                          width: '100%',
                          placeholder: 'Reader Name'
                        }),
                        beginButton: new sap.m.Button({
                          text: 'Submit',
                          press: function () {
                            var readerName = sap.ui.getCore().byId('submitDialogInput').getValue();
                            if (readerName){
                               this.fetchReaderBooks(readerName).then(data => this.getView().getModel().setProperty("/Books", data)).catch(console.error);
                            }
                            dialog.close();
                          }.bind(this)
                        }),
                        endButton: new sap.m.Button({
                          text: 'Cancel',
                          press: function () {
                            dialog.close();
                          }
                        }),
                        afterClose: function() {
                          dialog.destroy();
                        }
                    });
                    dialog.open();
                    break;
                case 'sortedTitle':
                    this.fetchSortedBooks("title").then(data => this.getView().getModel().setProperty("/Books", data)).catch(console.error);
                    break;
                case 'sortedAuthor':
                    this.fetchSortedBooks("author").then(data => this.getView().getModel().setProperty("/Books", data)).catch(console.error);
                    break;
            }
            this.byId("bookFilterSelect").setSelectedKey(null); 
        },

        onAddBook: function () {
            this.fetchResult({
                url: "/library/add-book",
                type: "POST",
                data: { title: this.getView().byId("bookTitle").getValue(), author: this.getView().byId("bookAuthor").getValue() },
                successMessage: "New Book added!",
                updateMethod: this.fetchBooks,
                listProp: "/Books",
                clearInputIds: ["bookTitle", "bookAuthor"]
            });
        },

        onSearchBooks: function (searchEvent) {
            $.ajax({
                type: "GET",
                url: `/library/search-books?query=${searchEvent.getParameter("query")}&type=${["title", "author"][this.getView().byId("searchType").getSelectedIndex()]}`,
                dataType: "json",
                success: data => {
                    this.getView().getModel().setProperty("/SearchResults", data);
                    MessageToast.show(data.length === 0 ? "No books found!" : "Search completed!");
                },
                error: err => MessageToast.show(err.toString()),
            });
        },

        onBorrowBook: function () {
            this.fetchResult({
                url: "/library/borrow-book",
                type: "PUT",
                data: {
                    readerName: this.getView().byId("borrowerName").getValue(),
                    bookTitle: this.getView().byId("borrowerBookTitle").getValue()
                },
                successMessage: "Book borrowed successfully!",
                updateMethods: [this.fetchBooks, this.fetchReaders],
                clearInputIds: ["borrowerName", "borrowerBookTitle"]
            });
        },

        onReturnBook: function () {
            this.fetchResult({
                url: "/library/return-book",
                type: "PUT",
                data: { 
                    readerName: this.getView().byId("returningReaderName").getValue(), 
                    bookTitle: this.getView().byId("returningBookTitle").getValue() 
                },
                successMessage: "Book returned successfully!",
                updateMethods: [this.fetchBooks, this.fetchReaders],
                clearInputIds: ["returningReaderName", "returningBookTitle"]
            });
        },

        onChangeLanguage: function (languageSelectEvent) {
            var selectedLanguage = languageSelectEvent.getParameter("selectedItem").getKey();
            var sUrl = window.location.href;
            
            if(sUrl.indexOf("sap-language") === -1)
                sUrl += (sUrl.indexOf("?") === -1 ? "?" : "&") + "sap-language=" + selectedLanguage;
            else
                sUrl = sUrl.replace(/(sap-language=).*?(&|$)/, '$1' + selectedLanguage + '$2');

            window.location.href = sUrl;
        },

        fetchReaders: function () {
            return this.fetchData("/library/readers");
        },

        fetchBooks: function () {
            return this.fetchData("/library/books");
        },

        fetchAvailableBooks: function () {
            return this.fetchData("/library/books/available");
        },

        fetchReaderBooks: function (readerName) {
            return this.fetchData("/library/books/reader?readerName=" + encodeURIComponent(readerName));
        },

        fetchSortedBooks: function (sortKey) {
            return this.fetchData("/library/books/sorted?sortKey=" + encodeURIComponent(sortKey));
        },

        fetchData: function (url) {
            return new Promise((resolve, reject) => {
                $.ajax({
                    type: "GET",
                    url,
                    dataType: "json",
                    success: resolve,
                    error: reject,
                });
            });
        },

        fetchResult: function({ url, type, data, successMessage, updateMethod, listProp, updateMethods=[], clearInputIds=[]}) {
            $.ajax({
                type,
                url,
                data,
                success: () => {
                    MessageToast.show(successMessage);
                    clearInputIds.forEach(inputId => this.getView().byId(inputId).setValue(""));
                    if(updateMethod)
                        updateMethod.call(this).then(data => this.getView().getModel().setProperty(listProp, data));
                    if(updateMethods.length > 0)
                        updateMethods.forEach(method => method.call(this));
                },
                error: () => { MessageToast.show('An error occurred.') }
            });
        },
    });
});