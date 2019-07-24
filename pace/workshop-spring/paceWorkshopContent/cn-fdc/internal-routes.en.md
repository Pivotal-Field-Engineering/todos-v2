### Internal Routes on PCF

We can restrict access to the Backend API and UI by removing public routes for those apps and then mapping them to an internal domain (``apps.internal``).  Once the apps have an internal route we can add a network policy that allows the Edge to call them.

1. Repeat pushing `YOUR-todos-api` and `YOUR-todos-webui` but this time set the [domain](https://docs.pivotal.io/pivotalcf/devguide/deploy-apps/routes-domains.html) to an internal one.

    ```bash
    # YOUR-todos-api
    cd ~/Desktop/todos-apps/todos-api
    cf domains
    > name                 status   details
    > apps.retro.io        shared          
    > mesh.apps.retro.io   shared          
    > apps.internal        shared   internal
    cf push your-todos-api -d apps.internal
    ```

    ```bash
    # YOUR-todos-webui
    cd ~/Desktop/todos-apps/todos-webui
    cf push your-todos-webui -d apps.internal
    ```

1. Use cf set-env to update endpoints on `YOUR-todos-edge` to the internal ones

    ```bash
    # YOUR-todos-edge
    cd ~/Desktop/todos-apps/todos-edge
    cf set-env your-todos-edge \
        TODOS_API_ENDPOINT http://your-todos-api.apps.internal:8080
    cf set-env your-todos-edge \
        TODOS_UI_ENDPOINT http://your-todos-webui.apps.internal:8080
    ```

1. Un-map public routes for `YOUR-todos-api` and `YOUR-todos-webui`

    ```bash
    # unmap the public routes for API and UI
    cd ~/Desktop/todos-apps/todos-edge
    cf unmap-route your-todos-api apps.retro.io --hostname your-todos-api
    cf unmap-route your-todos-webui apps.retro.io --hostname your-todos-webui
    ```

1. Restage `YOUR-todos-edge`

    ```bash
    cd ~/Desktop/todos-apps/todos-edge
    cf restage your-todos-edge
    ```

1. Add network policy to allow access to `YOUR-todos-api` and `YOUR-todos-webui` from only `YOUR-todos-edge`

    ```bash
    cd ~/Desktop/todos-apps/todos-edge
    cf add-network-policy your-todos-edge --destination-app your-todos-api
    cf add-network-policy your-todos-edge --destination-app your-todos-webui
    cf network-policies
    Listing network policies in org retro / space arcade as corbs...
    source       destination   protocol   ports
    todos-edge   todos-api     tcp        8080
    todos-edge   todos-webui   tcp        8080
    ```

1. Open `YOUR-todos-edge` in Chrome
    * You'll have a route to your `YOUR-todos-edge` app...for example `https://your-todos-edge.apps.retro.io`

### Extra mile

* Use [Todo(s) Shell](#todos-shell) to automate pushing the apps with private networking
    * `shell:>push-internal --tag myinternalapp`

### Talking Points

* Note that only your edge application can communicate with the API and UI and now those deployments aren't over exposed on the network.  This is accomplished by [Container to Container networking](https://docs.pivotal.io/pivotalcf/devguide/deploy-apps/cf-networking.html) in PCF.
