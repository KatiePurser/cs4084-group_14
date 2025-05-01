# cs4084-group_14 - Fashion Friend

## Project Rationale

This mobile application was developed as part of the CS4084: Mobile Application Development module at the University of Limerick. The primary objective of the project was to design and develop a full Android application that demonstrates a practical understanding of key mobile development concepts.

## Application Description

Fashion Friend is a mobile application that simulates a user’s wardrobe, making outfit planning simple and interactive. The app enables users to add clothing items to a virtual wardrobe, design complete outfits using those items, and set reminders to plan what to wear for upcoming events.

## Features

### Reminder System

The app includes a reminder system that helps users stay fashion-ready for upcoming events. When you add an event to your calendar, such as a party or interview, the app reminds you to prepare your outfit that morning.

### Clothing Item Management

Add clothing items to your virtual wardrobe. Select an image, specify the name, and choose the category for your item. Now you can use it when creating an outfit.

### Outfit Management

Create outfits from previously stored items in your wardrobe. You can view your complete outfit with its image, name, and a scrollable preview of all included clothing items. It allows you to modify outfit details including name, image, and the clothing items included in the outfit. Unwanted outfits from your wardrobe can be removed with a simple delete function.

### Wardrobe

The wardrobe holds all clothing items and outfits, dividing them into categories. From viewing outfits or clothing items, you can navigate to editing your existing items or creating new ones.

## Requirements

- Android Studio: Ladybug Feature Drop 2024.2.2
- Java Version: 11
- Gradle Plugin Version: 8.8
- Gradle Version: 8.10.2
- Compile SDK: 35
- Target SDK: 35
- Minimum SDK: 24

## How to Install & Run

To run this Android application locally, follow the steps below and make sure your environment meets the specified requirements.

1. Clone the repository into a local directory for use
2. Open it in Android Studio and ensure that Gradle syncs successfully. Once synced, allow the IDE to build the project automatically or trigger a manual build via **Build > Rebuild Project**
3. Create a new virtual device with the following specs:
   - **Device:** Medium phone
   - **System Image:** Vanilla Ice Cream (API level 35)
4. Launch the emulator and wait for it to boot
5. Run the application with the Android Green Run Button

## How to Use

### Reminders

- Open the main page of the app
- Tap on a date in the calendar to set a reminder
- You can navigate to future months using the calendar arrows
- A prompt will appear asking you to enter a short reminder message
- On the selected day, you’ll receive a notification from 9:00 AM with your reminder

### Clothing Items

- From the home page, click the ‘Add Item’ button
- Choose an image, specify the name and category for your item, and save it
- Go to the wardrobe, view your items, and edit them if necessary

### Creating a New Outfit

- Click "Add Outfit" from the home page or the “outfits” page from the wardrobe
- Select clothing items from categories
- Enter outfit name and select an image
- Tap "Save Outfit" to complete

### Viewing Outfits

- Go to "Outfits" tab in the wardrobe screen
- Tap any outfit to view details

### Editing an Outfit

- Open the outfit and tap "Edit Outfit"
- Change name, image, or tap "Edit Items" to modify clothing selections
- To delete, tap "Delete Outfit" and confirm

### Wardrobe

- Navigate to the Wardrobe activity using either the Wardrobe button on the home page or the dropdown menu in the toolbar
- Navigate to the category of your choice using the buttons in the side scroll view
- In this category you can:
  - Use the button at the top to add a new item to the category – this will redirect you to either add an outfit or add a clothing item, depending on which category you are viewing
  - Click on any of the items to edit them – this will again redirect you to the appropriate activity

## Contributions

The project was designed and implemented by the following team members:

- Sohaila Awaga (https://github.com/sohailaawaga)
- Oleksandr Kardash (https://github.com/oleksandr-kardash)
- Róisín Mitchell (https://github.com/RoisinMitchell)
- Katie Purser (https://github.com/KatiePurser)

## Developer Notes

### Gallery Images

Sample images are programmatically loaded into the emulator's gallery to simulate user photo content. These can be used when selecting outfit images within the app.

### Reminder Scheduling

A reminder is automatically added for the current day when the app is first launched. This is designed to showcase and validate the app’s reminder functionality in real time.

### Seeded Database

The project includes a pre-seeded local database. On first launch, the app loads with example data across all tables, providing ready-to-use outfits, events, and user entries for testing and demonstration.
