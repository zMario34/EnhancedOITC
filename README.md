# EnhancedOITC
An open-source, multiinstance-multiarena One In The Chamber minigame.
Requires a MySQL and a Redis server to function properly. You need to use a proxy (like Velocity or BungeeCord) to switch server and arena to the player

## Building
We use Maven to handle dependencies & building.

### Requirements
To compile the project, you must have: JDK 11, Git and Maven

#### Compiling from source
Run the following command from your command line:
```
git clone https://github.com/zmario34/EnhancedOITC.git
cd EnhancedOITC
mvn clean install
```

You can find the output JARs in the `target` directories of every module.

## Contributing
#### Pull Requests
If you make any changes or improvements to the plugin which you think would be beneficial to others, please consider making a pull request.

#### Project Structure
The project is split up into a few separate modules.

* **Common** - The common module contains some code for both game and connector modules.
* **Game** - The plugin module which contains the operation of the game itself (for slave servers)
* **Connector** - The lobby plugin module which contains the code to allow connection in the various arenas
