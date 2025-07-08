// ==UserScript==
// @name         Source Downloader
// @namespace    http://npc.com/
// @version      2025-06-09
// @description  Suppor Automatic Source Saving
// @match        https://uni.open2ch.net/test/read.cgi/gameswf/*
// ==/UserScript==

(function() {
    'use strict';

    if (location.hash === "#audit") {
         // get the current source
        const html = document.documentElement.outerHTML;

        fetch("http://localhost:13849", {
            method: "POST",
            headers: {
                "Content-Type": "text/html",
            },
            body: html
        }).then(response => {
            console.log("POST successful:", response.status);
            window.close();
        }).catch(error => {
            console.log("POST failed:", error);
        });
    }
})();