/**
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
 */

const CONFIG = document.getElementById("config");
const LANGS = document.getElementById("languages");
const CUSTOM_LANG = document.getElementById("custom_language");

// Checks the latest version of Taterzens
const versionCheck = new XMLHttpRequest();
versionCheck.onreadystatechange = () => {
    if (versionCheck.readyState == 4 && versionCheck.status == 200) { //
        let version = versionCheck.responseText;
        version = JSON.parse(version);
        version = version.tag_name;
        checkCache(version);
    }
};

versionCheck.open("GET", "https://api.github.com/repos/samolego/Taterzens/releases/latest", true);
versionCheck.send();


async function checkCache(version) {
    let configCache = JSON.parse(localStorage.getItem("ConfigCache"));
    let langCache = JSON.parse(localStorage.getItem("LangsCache"));
    let CACHED_VERSION = JSON.parse(localStorage.getItem("TaterzensVersion"));

    if(configCache == null || langCache == null || CACHED_VERSION != version) {
        fetchNewData(version);
    } else {
        CONFIG.innerHTML = configCache;
        setLangs(langCache);
    }
}

async function fetchNewData(version) {
    const xhttpConfig = new XMLHttpRequest();
    let processedConfig = false;

    xhttpConfig.onreadystatechange = () => {
        if(xhttpConfig.responseText != "" && !processedConfig) {
            processedConfig = true;

            // Sets the config
            setConfig(xhttpConfig.responseText);

            localStorage.setItem("ConfigCache", JSON.stringify(CONFIG.innerHTML));
            localStorage.setItem("TaterzensVersion", JSON.stringify(version));
        }
    };

    xhttpConfig.open("GET", `https://raw.githubusercontent.com/samolego/Taterzens/${version}/common/src/main/java/org/samo_lego/taterzens/storage/TaterConfig.java`, true);
    xhttpConfig.send();


    const xhttpLangs = new XMLHttpRequest();
    let processedLangs = false;

    xhttpLangs.onreadystatechange = () => {
        if(xhttpLangs.responseText != "" && !processedLangs) {
            processedLangs = true;
            // Sets the langs
            const langs = JSON.parse(xhttpLangs.responseText);
            let parsed = [];

            langs.forEach((l) => {
                parsed.push(l.name.substring(0, ".json".length));
            });
            localStorage.setItem("LangsCache", JSON.stringify(parsed));

            setLangs(parsed);
        }
    };

    xhttpLangs.open("GET", `https://api.github.com/repos/samolego/taterzens/contents/common/src/main/resources/data/taterzens/lang`, true);
    xhttpLangs.send();
}

async function setLangs(langs) {
    LANGS.innerHTML = "";
    langs.forEach((l) => {
        const LI = document.createElement("LI");
        LI.innerHTML = l;

        LANGS.appendChild(LI);
    });
    const lang_count = langs.length;
    document.getElementById("lang_count").innerHTML = `Available languages (${lang_count})`;
    setRandomLang(langs);
}

async function setRandomLang(langJsons) {
    const en_us = langJsons.indexOf("en_us");
    langJsons.splice(en_us, 1);
    const rnd = Math.floor(Math.random() * langJsons.length);
    const rndLang = langJsons[rnd];
    console.log(rndLang);
    CUSTOM_LANG.innerHTML = `+  "language": "${rndLang}"`;
}

async function setConfig(configText) {
    CONFIG.innerHTML = "";
    let i = j = 0;
    let level = -1;
    let previousLine = "";

    // Reading by lines
    while ((j = configText.indexOf("\n", i)) !== -1) {
        let line = configText.substring(i, j);

        if(line != "" && !line.startsWith("i")) {
            level += (line.match(/\{/g)  || []).length;
            if(line.includes("public final String _comment")) {
                // Comment line
                const start = line.lastIndexOf(" = \"");
                // + 7 to not include String & space
                const COMMENT = document.createElement("SPAN");
                COMMENT.style = "color: #6a8759;";

                // Getting name from @SerializedName
                if(previousLine != "") {
                    COMMENT.innerHTML = "    ".repeat(level) + previousLine;
                    previousLine = "";
                } else {
                    COMMENT.innerHTML = "    ".repeat(level) + line.substring(start + 4, line.length - 2);
                }

                CONFIG.appendChild(COMMENT);
                CONFIG.innerHTML += "\n";
            } else if(line.includes("public static class ")) {
                const SECTION = document.createElement("SPAN");
                SECTION.style = "color: #2c84fa; font-size: large;";

                let start = line.indexOf("public static class ");
                let option = line.substring(start + 20, line.indexOf(" {")).toUpperCase();

                SECTION.innerHTML = "    ".repeat(level - 1) + option;

                CONFIG.innerHTML += "\n";
                CONFIG.appendChild(SECTION);
                CONFIG.innerHTML += "\n";
            } else if(line.includes("public ") && line.includes("=") && line.includes(";") && !line.includes("new")) {
                // Option line
                let start = line.indexOf("public ");
                let option = line.substring(start + 7, line.length - 1);
                start = option.indexOf(" ");

                const TYPE = document.createElement("SPAN");
                TYPE.style = "color: orange;";
                TYPE.innerHTML = option.substring(0, start);

                option = option.substring(start + 1, option.length);

                let spaceIndex = option.indexOf(" ");

                const KEY = document.createElement("SPAN");
                KEY.innerHTML = "    ".repeat(level) + option.substring(0, spaceIndex);
                KEY.style = "color: red;";

                // Getting name from @SerializedName
                if(previousLine != "") {
                    KEY.innerHTML = "    ".repeat(level) + previousLine;
                    previousLine = "";
                }


                let value = option.substring(spaceIndex, option.length);

                CONFIG.appendChild(KEY);
                CONFIG.innerHTML += ": ";
                CONFIG.appendChild(TYPE);
                CONFIG.innerHTML += value + "\n";
            } else if(line.includes("@SerializedName")) {
                let start = line.indexOf("\"") + 1;
                let end = line.lastIndexOf("\"");
                previousLine = line.substring(start, end);
            }
            level -= (line.match(/\}/g)  || []).length;

        }
        i = j + 1;
    }
}

