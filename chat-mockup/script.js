// --- Dynamic Database for Chats ---
const chatData = {};

let currentContact = null;
let stompClient = null;
let token = localStorage.getItem('chat_jwt_token');

// Prompt for token if not stored
if (!token) {
    token = prompt("Please enter your JWT Authorization Token (Bearer):");
    if (token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        localStorage.setItem('chat_jwt_token', token);
    }
}

// Extract current user ID from JWT token payload (optional helper)
let currentUserId = 1;
if (token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.id) {
            currentUserId = payload.id;
        }
    } catch (e) {
        console.warn("Could not parse JWT payload to extract user ID, defaulting to ID 1.");
    }
}

// --- DOM Elements ---
const messagesContainer = document.getElementById("messagesContainer");
const messageInput = document.getElementById("messageInput");
const sendBtn = document.getElementById("sendBtn");
const conversationsList = document.getElementById("conversationsList");

// --- Chat Header Elements ---
const chatHeaderAvatar = document.querySelector(".chat-header .avatar");
const chatHeaderName = document.querySelector(".chat-header .contact-name");
const chatHeaderStatus = document.querySelector(".chat-header .contact-status");
const chatHeaderStatusDot = document.querySelector(".chat-header .status-dot");

// --- Helper Functions ---
function getFormattedTime() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
}

function formatTime(dateTimeStr) {
    if (!dateTimeStr) return getFormattedTime();
    try {
        const date = new Date(dateTimeStr);
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes}`;
    } catch (e) {
        return getFormattedTime();
    }
}

function scrollToBottom() {
    messagesContainer.scrollTo({
        top: messagesContainer.scrollHeight,
        behavior: 'smooth'
    });
}

// --- Render Active Chat Box ---
function renderChat(contactName) {
    const data = chatData[contactName];
    if (!data) {
        messagesContainer.innerHTML = `<div class="date-divider">Select a contact to start messaging</div>`;
        return;
    }
    
    // Update Header
    chatHeaderAvatar.src = data.avatar;
    chatHeaderName.textContent = contactName;
    chatHeaderStatus.textContent = data.status;
    
    if (data.isOnline) {
        chatHeaderStatus.style.color = "#10b981";
        chatHeaderStatusDot.className = "status-dot online";
    } else {
        chatHeaderStatus.style.color = "#6b7280";
        chatHeaderStatusDot.className = "status-dot offline";
    }
    
    // Clear Messages
    messagesContainer.innerHTML = `<div class="date-divider">Today</div>`;
    
    // Render History
    data.messages.forEach(msg => {
        const msgDiv = document.createElement("div");
        msgDiv.className = `message ${msg.type}`;
        
        if (msg.type === "incoming") {
            msgDiv.innerHTML = `
                <img src="${data.avatar}" alt="${contactName}" class="message-avatar">
                <div class="message-bubble-wrapper">
                    <div class="message-bubble">${msg.text}</div>
                    <span class="message-meta">${msg.time}</span>
                </div>
            `;
        } else {
            msgDiv.innerHTML = `
                <div class="message-bubble-wrapper">
                    <div class="message-bubble">${msg.text}</div>
                    <span class="message-meta">${msg.time} <i class="fa-solid fa-check-double read-receipt"></i></span>
                </div>
            `;
        }
        messagesContainer.appendChild(msgDiv);
    });
    
    scrollToBottom();
}

// --- Render Sidebar Conversation List ---
function renderConversationList() {
    conversationsList.innerHTML = "";
    
    Object.keys(chatData).forEach(name => {
        const contact = chatData[name];
        const itemDiv = document.createElement("div");
        itemDiv.className = `conversation-item ${name === currentContact ? 'active' : ''}`;
        
        // Find last message in array
        const lastMsgObj = contact.messages[contact.messages.length - 1];
        const lastMsgText = lastMsgObj ? lastMsgObj.text : "No messages yet";
        const lastMsgTime = lastMsgObj ? lastMsgObj.time : "";
        
        itemDiv.innerHTML = `
            <div class="avatar-container">
                <img src="${contact.avatar}" alt="${name}" class="avatar">
                <span class="status-dot ${contact.isOnline ? 'online' : 'offline'}"></span>
            </div>
            <div class="conversation-details">
                <div class="conversation-header">
                    <span class="contact-name">${name}</span>
                    <span class="message-time">${lastMsgTime}</span>
                </div>
                <div class="conversation-footer">
                    <span class="last-message">${lastMsgText}</span>
                    ${contact.unreadCount > 0 ? `<span class="unread-badge animate-badge">${contact.unreadCount}</span>` : ""}
                </div>
            </div>
        `;
        
        itemDiv.addEventListener("click", () => {
            document.querySelectorAll(".conversation-item").forEach(i => i.classList.remove("active"));
            itemDiv.classList.add("active");
            
            const badge = itemDiv.querySelector(".unread-badge");
            if (badge) badge.remove();
            
            currentContact = name;
            contact.unreadCount = 0;
            loadHistoryFromServer(currentContact);
        });
        
        conversationsList.appendChild(itemDiv);
    });
}

// --- REST API: Fetch Users from DB ---
function fetchDatabaseUsers() {
    if (!token) return;
    
    fetch('http://localhost:8080/users', {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(res => {
        if (res.status === 401 || res.status === 403) {
            localStorage.removeItem('chat_jwt_token');
            alert("Session expired or unauthorized. Please reload to login.");
            return;
        }
        return res.json();
    })
    .then(users => {
        if (!users || users.length === 0) {
            conversationsList.innerHTML = `<div style="padding:20px;color:var(--text-muted);font-size:0.85rem;">No other users in database</div>`;
            return;
        }
        
        // Build chatData mapping dynamically from database users
        users.forEach(u => {
            const username = u.username;
            chatData[username] = {
                id: u.id,
                // Assign a dynamic unique bot avatar from dicebear
                avatar: `https://api.dicebear.com/7.x/bottts/svg?seed=${username}`,
                status: u.enabled ? "Active Now" : "Offline",
                isOnline: u.enabled,
                unreadCount: 0,
                messages: [],
                replies: []
            };
        });
        
        // Set first contact active by default
        const contacts = Object.keys(chatData);
        if (contacts.length > 0) {
            currentContact = contacts[0];
        }
        
        renderConversationList();
        if (currentContact) {
            loadHistoryFromServer(currentContact);
        }
    })
    .catch(err => {
        console.error("Error loading database users:", err);
        conversationsList.innerHTML = `<div style="padding:20px;color:red;font-size:0.85rem;">Error connecting to API</div>`;
    });
}

// --- REST API: Load History from Java Backend ---
function loadHistoryFromServer(contactName) {
    const data = chatData[contactName];
    if (!data || !token) return;
    
    fetch(`http://localhost:8080/messages/${data.id}?page=0&size=50`, {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(res => {
        if (res.status === 401 || res.status === 403) {
            localStorage.removeItem('chat_jwt_token');
            alert("Session expired or unauthorized. Please reload to log in.");
            return;
        }
        return res.json();
    })
    .then(resPage => {
        if (!resPage) return;
        const backendMessages = resPage.content.map(m => {
            const isOutgoing = m.sender.id === currentUserId;
            return {
                type: isOutgoing ? "outgoing" : "incoming",
                text: m.content,
                time: formatTime(m.localDateTime)
            };
        }).reverse(); // Display oldest first
        
        data.messages = backendMessages;
        renderChat(contactName);
        renderConversationList(); // Update sidebar stamps/last messages
    })
    .catch(err => {
        console.error("Error loading chat history:", err);
        renderChat(contactName);
    });
}

let reconnectTimeout = null;

// --- WebSocket: Connect to Spring Boot ---
function connectWebSocket() {
    if (!token) return;
    
    if (stompClient && stompClient.connected) return;
    
    if (reconnectTimeout) {
        clearTimeout(reconnectTimeout);
        reconnectTimeout = null;
    }
    
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({
        'Authorization': 'Bearer ' + token
    }, onConnected, onError);
}

function onConnected() {
    console.log("Connected to WebSocket message broker!");
    stompClient.subscribe('/user/queue/messages', onMessageReceived);
}

function onError(error) {
    console.error("WebSocket connection error:", error);
    if (!reconnectTimeout) {
        reconnectTimeout = setTimeout(() => {
            reconnectTimeout = null;
            connectWebSocket();
        }, 5000);
    }
}

// --- WebSocket: Incoming Message Handler ---
function onMessageReceived(message) {
    const msg = JSON.parse(message.body);
    const time = formatTime(msg.time);
    
    const senderName = Object.keys(chatData).find(name => chatData[name].id === msg.senderId) || msg.senderName;
    const contact = chatData[senderName];
    
    if (!contact) return;
    
    contact.messages.push({
        type: "incoming",
        text: msg.content,
        time: time
    });
    
    if (currentContact === senderName) {
        const msgDiv = document.createElement("div");
        msgDiv.className = "message incoming";
        msgDiv.innerHTML = `
            <img src="${contact.avatar}" alt="${senderName}" class="message-avatar">
            <div class="message-bubble-wrapper">
                <div class="message-bubble">${msg.content}</div>
                <span class="message-meta">${time}</span>
            </div>
        `;
        messagesContainer.appendChild(msgDiv);
        scrollToBottom();
    } else {
        contact.unreadCount = (contact.unreadCount || 0) + 1;
        playNotificationSound();
    }
    
    renderConversationList();
    
    // Animate item bounce if inactive
    if (senderName !== currentContact) {
        const items = document.querySelectorAll(".conversation-item");
        items.forEach(item => {
            const name = item.querySelector(".contact-name").textContent;
            if (name === senderName) {
                item.classList.add("notify-bounce");
                setTimeout(() => item.classList.remove("notify-bounce"), 600);
            }
        });
    }
}

// --- WebSocket: Sending a Message ---
function sendMessage() {
    const text = messageInput.value.trim();
    if (!text || !currentContact) return;
    
    const time = getFormattedTime();
    
    if (stompClient && stompClient.connected) {
        const payload = {
            recipientId: chatData[currentContact].id,
            content: text
        };
        stompClient.send("/app/chat", {}, JSON.stringify(payload));
        
        chatData[currentContact].messages.push({
            type: "outgoing",
            text: text,
            time: time
        });
        
        const msgDiv = document.createElement("div");
        msgDiv.className = "message outgoing";
        msgDiv.innerHTML = `
            <div class="message-bubble-wrapper">
                <div class="message-bubble">${text}</div>
                <span class="message-meta">${time} <i class="fa-solid fa-check-double read-receipt"></i></span>
            </div>
        `;
        messagesContainer.appendChild(msgDiv);
        
        renderConversationList();
        messageInput.value = "";
        scrollToBottom();
    } else {
        alert("Not connected to chat server. Attempting reconnect...");
        connectWebSocket();
    }
}

// --- Play Premium Notification Sound ---
function playNotificationSound() {
    try {
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const playChime = (time, pitch) => {
            const osc = audioCtx.createOscillator();
            const gain = audioCtx.createGain();
            
            osc.connect(gain);
            gain.connect(audioCtx.destination);
            
            osc.type = "sine";
            osc.frequency.setValueAtTime(pitch, time);
            
            gain.gain.setValueAtTime(0.08, time);
            gain.gain.exponentialRampToValueAtTime(0.001, time + 0.25);
            
            osc.start(time);
            osc.stop(time + 0.25);
        };
        
        const now = audioCtx.currentTime;
        playChime(now, 659.25); // E5
        playChime(now + 0.08, 987.77); // B5
    } catch (e) {
        console.warn("AudioContext warning:", e);
    }
}

// --- Event Listeners ---
sendBtn.addEventListener("click", sendMessage);

messageInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        sendMessage();
    }
});

// --- Initial Connect & Render ---
window.addEventListener("DOMContentLoaded", () => {
    connectWebSocket();
    fetchDatabaseUsers();
});
