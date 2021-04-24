# AutoLockdown

This is a quick (by quick I mean around 14 hours of work) app for me to stop being a caveman with my privacy-paranoid ways of using a 14 pin password that is prone to input error and infinite frustrations instead of a well-supported biometric methods.

Biometric methods are susceptible to social engineering, improper security and an assortment of paranoia-related concerns. To retain the effectiveness of unlocking an Android phone with a fingerprint, yet still maintaining some level of security and privacy, I implemented an automatic way to turn off biometric authentication after a customizable grace period.

## Example Scenario

You are tired. You hold your phone protected by the latest ultrasonic fingerprint sensor that is all the hype these days. You fall asleep. A sneaky adversary quietly swipes your phone, and holds out your finger to unlock the phone.

Just like that, all your personal information have been swiftly stolen from your very hands.

With a PIN/Password, the adversary will simply walk away with a phone with little options to access the data within; however, you fumble with your phone in frequent, 5 minute intervals and find it a hassle to unlock your phone every time. With AutoLockdown, you will have a grace period of 5 minutes after you lock your phone to authenticate yourself with fingerprint, after which only a PIN/Password will be allowed.

# Why API 28

Google deprecates `KEYGUARD_DISABLE_FINGERPRINT` with no equivalent properties for our use. Check this thread for more information: https://stackoverflow.com/questions/55184173/setkeyguarddisabledfeaturescomponentname-devicepolicymanager-keyguard-disable
