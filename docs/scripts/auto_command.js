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

let COMMAND_FIELD = document.getElementById("npcCommand");
let command = "NpcCommand";
if(COMMAND_FIELD == null) {
    COMMAND_FIELD = document.getElementById("taterzensCommand");
    command = "TaterzensCommand";
}

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

versionCheck.onerror = () => {
    checkCache(JSON.parse(localStorage.getItem("TaterzensVersion")));
};


async function checkCache(version) {
    let cache = JSON.parse(localStorage.getItem(command + "Cache"));
    const CACHED_VERSION = JSON.parse(localStorage.getItem("TaterzensVersion"));
    if(cache == null || CACHED_VERSION != version) {
        generateCommandLayout(version);
    } else {
        COMMAND_FIELD.innerHTML = cache;
    }
}

async function generateCommandLayout(version) {
    const xhttp = new XMLHttpRequest();
    let processed = false;

    xhttp.onreadystatechange = () => {
        if(xhttp.responseText != "" && !processed) {
            processed = true;
            COMMAND_FIELD.innerHTML = "";

            let configText = xhttp.responseText;
            let i = level = j = 0;
            let inRegisterMethod = false;

            // Reading by lines
            while ((j = configText.indexOf("\n", i)) !== -1) {
                let line = configText.substring(i, j);

                if(line != "" && !line.startsWith("i")) {
                    if(line.includes("public static void register(")) {
                        inRegisterMethod = true;
                    } else if(line.includes("}") && inRegisterMethod && level == 0) {
                        break;
                    }
                    if(inRegisterMethod) {
                        level += (line.match(/\(/g)  || []).length;

                        if(line.includes("PERMISSIONS.")) {
                            let start = line.indexOf("PERMISSIONS.") + 12;
                            let end = line.lastIndexOf(", ");
                            const PERM = document.createElement("SPAN");
                            PERM.innerHTML = " (permission: " + line.substring(start, end) + ")";
                            PERM.style = "color: cyan;";
                            COMMAND_FIELD.appendChild(PERM);
                        }
    
                        if(line.includes("literal(")) {
                            let start = line.indexOf("(\"") + 2;
                            let end = line.indexOf("\")");
    
                            let literal = line.substring(start, end);

                            const NODE = document.createElement("SPAN");
                            NODE.innerHTML = "\n" + "    ".repeat(level) + "-&gt; " + literal;
        
                            COMMAND_FIELD.appendChild(NODE);
                        }
                        if(line.includes("argument(")) {
                            let start = line.indexOf("(\"") + 2;
                            let end = line.indexOf("\",");
    
                            let argument = line.substring(start, end);

                            const ARG = document.createElement("SPAN");
                            ARG.innerHTML = "\n" + "    ".repeat(level) + "&lt;" + argument + "&gt;";
                            ARG.style = "color: lightgreen;";
        
                            COMMAND_FIELD.appendChild(ARG);
                        }
                    }

                    level -= (line.match(/\)/g)  || []).length

                }
                i = j + 1;
            }

            localStorage.setItem(command + "Cache", JSON.stringify(COMMAND_FIELD.innerHTML));
            localStorage.setItem("TaterzensVersion", JSON.stringify(version));
        }
    };

    xhttp.open("GET", `https://raw.githubusercontent.com/samolego/Taterzens/${version}/common/src/main/java/org/samo_lego/taterzens/commands/${command}.java`, true);
    xhttp.send();

}

