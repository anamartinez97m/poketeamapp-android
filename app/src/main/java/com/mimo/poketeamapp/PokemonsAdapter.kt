package com.mimo.poketeamapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.mimo.poketeamapp.database.AppDatabase
import com.mimo.poketeamapp.databinding.PokemonItemBinding
import com.mimo.poketeamapp.model.Pokemon
import com.squareup.picasso.Picasso

class PokemonsAdapter(private var pokemons: List<Pokemon>): RecyclerView.Adapter<PokemonsAdapter.PokemonViewHolder>() {

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val item = pokemons[position]
        holder.bind(item, this, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PokemonViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.pokemon_item, parent, false), parent
    )

    override fun getItemCount() = pokemons.size

    class PokemonViewHolder(
        view: View,
        private val parent: ViewGroup
    ): RecyclerView.ViewHolder(view) {
        private val binding = PokemonItemBinding.bind(view)
        private val add = binding.add
        private val remove = binding.remove
        private val pokemonName = binding.pokemonName
        private val pokemonBaseExperience = binding.pokemonBaseExperience
        private val pokemonImage = binding.pokemonImage

        val db = Room
            .databaseBuilder(parent.context, AppDatabase::class.java, "pokemon-database")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        @SuppressLint("NotifyDataSetChanged")
        fun bind(
            pokemon: Pokemon,
            pokemonsAdapter: PokemonsAdapter,
            position: Int
        ) {
            if(parent.context is PokeTeamActivity) {
                add.visibility = View.GONE
            } else {
                remove.visibility = View.GONE
            }
            pokemonName.text = pokemon.name
            pokemonBaseExperience.text = pokemon.base_experience.toString()
            if(pokemon.sprites == null && pokemon.image != null && pokemon.image.isNotEmpty()) {
                Picasso.get().load(pokemon.image).into(pokemonImage)
            } else {
                Picasso.get().load(pokemon.sprites?.other?.home?.front_default).into(pokemonImage)
            }

            remove.setOnClickListener {
                db.pokemonDao().removeFavorite(pokemon.id)
                pokemonsAdapter.pokemons = pokemonsAdapter.pokemons.minus(pokemon)
                pokemonsAdapter.notifyItemRemoved(position)
                pokemonsAdapter.notifyItemRangeRemoved(position, pokemonsAdapter.itemCount)
            }

            add.setOnClickListener {
                val count = db.pokemonDao().getFavoritesCount()
                if(count < maxPokemonsToBeFavorites ) {
                    if(db.pokemonDao().isPokemonFavorite(pokemon.id)) {
                        Toast.makeText(parent.context, R.string.already_favorite, Toast.LENGTH_SHORT).show()
                    } else {
                        if (pokemon.url != null) {
                            db.pokemonDao().addFavorite(
                                pokemon.name,
                                pokemon.url,
                                pokemon.id,
                                pokemon.sprites?.other?.home?.front_default!!,
                                pokemon.base_experience)
                        } else {
                            db.pokemonDao().addFavorite(
                                pokemon.name,
                                "",
                                pokemon.id,
                                pokemon.sprites?.other?.home?.front_default!!,
                                pokemon.base_experience)
                        }
                        Toast.makeText(parent.context, R.string.added_favorite, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(parent.context, R.string.favorite_max_reached, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}