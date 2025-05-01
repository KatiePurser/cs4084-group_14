# cs4084-group_14 - Fashion Friend

## Project Rationale

This mobile application was developed as part of the **CS4084: Mobile Application Development** module at the **University of Limerick**. The primary objective of the project was to design and develop a full Android application that demonstrates a practical understanding of key mobile development concepts.

## Application Description

**Fashion Friend** is a mobile application that simulates a user’s wardrobe, making outfit planning simple and interactive. The app enables users to add clothing items to a virtual wardrobe, design complete outfits using those items, and set reminders to plan what to wear for upcoming events.

## Features

### Reminder System

The app includes a reminder system that helps users stay fashion-ready for upcoming events. When you add an event to your calendar such as a party or interview, the app reminds you to prepare your outfit that morning.

### Clothing Item Management

Add clothing items to your virtual wardrobe. Select an image, specify the name, and choose the category for your item. You can then use it when creating an outfit.

### Outfit Management

Create outfits from previously stored items in your wardrobe. View your complete outfit with its image, name, and a scrollable preview of included items. You can modify outfit details (name, image, items), and delete outfits when no longer needed.

### Wardrobe

The wardrobe holds all clothing items and outfits, organised by category. From there, you can view, edit, or create new items and outfits.

## Requirements

- **Android Studio**: Ladybug Feature Drop 2024.2.2
- **Java Version**: 11
- **Gradle Plugin Version**: 8.8
- **Gradle Version**: 8.10.2
- **Compile SDK**: 35
- **Target SDK**: 35
- **Minimum SDK**: 24

## How to Install & Run

To run this Android application locally, follow the steps below and ensure your environment meets the requirements:

1. **Clone the repository** into a local directory.
2. **Open the project** in Android Studio.
3. Allow **Gradle to sync**, then go to **Build > Rebuild Project**.
4. Create a new **virtual device** with:
   - Device: Medium phone
   - System Image: _Vanilla Ice Cream_ (API level 35)
5. **Launch the emulator** and wait for it to boot.
6. Run the application using the green **Run** button in Android Studio.

## How to Use

### Reminders

- Open the main page of the app.
- Tap on a date in the calendar to set a reminder.
- Navigate to future months using the arrows if needed.
- Enter a short reminder message when prompted.
- You’ll receive a notification at **9:00 AM** on the selected day.

### Clothing Items

- Tap the **‘Add Item’** button on the home page.
- Choose an image, enter a name and category, then save the item.
- View and edit your items from the **Wardrobe**.

### Creating a New Outfit

- Tap **'Add Outfit'** from the home or outfits page.
- Select clothing items from various categories.
- Enter an outfit name and choose an image.
- Tap **"Save Outfit"** to complete the process.

### Viewing Outfits

- Navigate to the **'Outfits'** tab in the wardrobe screen.
- Tap an outfit to view its details.

### Editing an Outfit

- Open the outfit and tap **'Edit Outfit'**.
- Change the name, image, or clothing items.
- To delete, tap **'Delete Outfit'** and confirm.

### Wardrobe

- Navigate to the wardrobe using the **Wardrobe** button or the toolbar dropdown.
- Use the scrollable category view to find clothing types.
- Tap the top button to **add a new item or outfit** based on the category.
- Tap any item to edit it — you'll be redirected to the appropriate screen.

## Contributions

The project was designed and implemented by the following team members:

- [Sohaila Awaga](https://github.com/sohailaawaga)
- [Oleksandr Kardash](https://github.com/oleksandr-kardash)
- [Róisín Mitchell](https://github.com/RoisinMitchell)
- [Katie Purser](https://github.com/KatiePurser)

## Developer Notes

- **Gallery Images**  
  Sample images are programmatically loaded into the emulator's gallery to simulate user photo content. These can be used when selecting outfit images.

- **Reminder Scheduling**  
  A reminder is automatically added for the current day when the app is first launched. This is to showcase the reminder functionality.

- **Seeded Database**  
  The app includes a pre-seeded local database. On first launch, example data is loaded across all tables to enable quick testing of outfits, events, and wardrobe entries.
