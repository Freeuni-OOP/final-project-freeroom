# Notification System Guide

This guide is for the frontend team to understand how to trigger and use the new global notification system. 

## 1. Setup

First, import the hook into any component where you need to trigger a notification:

```javascript
import { useNotification } from '@/context';

const MyComponent = () => {
  const { showNotification } = useNotification();
  // ...
}
```

## 2. Notification Types

There are four types of notifications available. They slide in from the bottom-right and automatically disappear (standard notifications last 4 seconds, action notifications last 10 seconds).

### Success Notification
Use this for successful operations (e.g., room reserved, profile saved).
```javascript
showNotification({ 
  message: 'ოთახი 108 წარმატებით დაიჯავშნა 30 წუთით', 
  type: 'success' 
});
```

### Error Notification
Use this for displaying backend errors or validation failures.
```javascript
showNotification({ 
  message: 'დაჯავშნა ვერ მოხერხდა: ოთახი უკვე დაკავებულია', 
  type: 'error' 
});
```

### Info Notification
Use this for general information. (If you don't provide a `type`, it defaults to `info`).
```javascript
showNotification({ 
  message: 'თქვენი სესია მალე ამოიწურება', 
  type: 'info' 
});
```

### Action Notification (Friend Requests)
Use this for interactive notifications like incoming friend requests. It includes "დამტკიცება" (Accept) and "უარყოფა" (Reject) buttons.

```javascript
showNotification({
  message: 'ნიკამ გამოგიგზავნათ მეგობრობის მოთხოვნა',
  type: 'action',
  duration: 15000, // Optional: customize how long it stays (in ms)
  onAccept: () => {
    console.log('User clicked Accept');
    // Call your API to accept the friend request here
  },
  onReject: () => {
    console.log('User clicked Reject');
    // Call your API to reject the friend request here
  }
});
```

## 3. Advanced Overrides

You can manually control the auto-dismiss time by passing a `duration` (in milliseconds). Set `duration: 0` if you want it to stay permanently until the user clicks the X button or an action button.

```javascript
showNotification({
  message: 'ეს შეტყობინება არ გაქრება სანამ არ გათიშავთ.',
  type: 'info',
  duration: 0
});
```
