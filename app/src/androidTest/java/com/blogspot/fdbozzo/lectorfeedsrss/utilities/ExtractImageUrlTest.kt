/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demo.rssfeedreader.utilities

//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.ItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.GroupDao
import com.blogspot.fdbozzo.lectorfeedsrss.util.getSrcImage
import kotlinx.coroutines.runBlocking
import org.junit.*

class ItemDaoTableTests {

    //@get:Rule
    //val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var itemDao: ItemDao
    private lateinit var db: FeedDatabase

    @Before
    fun setup() {
        // TODO: setup
    }

    @After
    fun tearDown() {
        // TODO: tearDown
    }

    @Test
    fun deberiaExtraerLaUrlDeLaPriemrImagenDelTexto() = runBlocking {
        // Texto origen
        val text = "<![CDATA[<p><img width=\"1024\" height=\"537\" " +
                "src=\"https://hardzone.es/app/uploads-hardzone.es/2020/11/DMA-Portada-1024x537.jpg\" " +
                "class=\"attachment-large size-large wp-post-image\" alt=\"DMA Portada\" loading=\"lazy\" " +
                "srcset=\"https://hardzone.es/app/uploads-hardzone.es/2020/11/DMA-Portada-1024x537.jpg " +
                "1024w, https://hardzone.es/app/uploads-hardzone.es/2020/11/DMA-Portada-300x157.jpg 300w, " +
                "https://hardzone.es/app/uploads-hardzone.es/2020/11/DMA-Portada-768x402.jpg 768w, " +
                "https://hardzone.es/app/uploads-hardzone.es/2020/11/DMA-Portada.jpg 1267w\" " +
                "sizes=\"(max-width: 1024px) 100vw, 1024px\" />Las unidades DMA se encuentran en todo " +
                "tipo de hardware, se trata de una de las piezas fundamentales para comunicar los " +
                "periféricos entre sí y con la memoria principal. Mientras que la IOMMU se encarga de " +
                "gestionar el direccionamiento es la unidad DMA la que hace el trabajo de transferir datos " +
                "desde la memoria a [&#8230;]</p>\n" +
                "<p>The post <a rel=\"nofollow\" href=\"https://hardzone.es/reportajes/que-es/unidades-dma" +
                "/\">¿Qué es, cómo funciona una unidad DMA y cuál es su utilidad?</a> appeared first on " +
                "<a rel=\"nofollow\" href=\"https://hardzone.es\">HardZone</a>.</p>\n" +
                "]]>"

        Assert.assertEquals(
            "https://hardzone.es/app/uploads-hardzone.es/2020/11/DMA-Portada-1024x537.jpg",
            getSrcImage(text)
        )
    }

}